package com.sharazan.http.server

import com.sharazan.core.withContext
import com.sharazan.http.core.error
import com.sharazan.http.handler.CoroutineHttpHandler
import com.sharazan.logging.METHOD_MDC_KEY
import com.sharazan.logging.PATH_MDC_KEY
import com.sharazan.logging.REQUEST_ID_MDC_KEY
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.sync.Semaphore
import org.http4k.core.*
import org.slf4j.LoggerFactory
import java.util.UUID

class CoroutineChannelHandler(
    private val handler: CoroutineHttpHandler,
    private val serverScope: CoroutineScope,
): SimpleChannelInboundHandler<FullHttpRequest>() {

    private val logger = LoggerFactory.getLogger(CoroutineChannelHandler::class.java)

    private val limiter = Semaphore(100)

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: FullHttpRequest
    ) {
        if (!limiter.tryAcquire()) {
            logger.warn("Too many requests, backpressure limit reached")

            ctx.writeAndFlush(error("Too many requests").toNettyResponse())
            return
        }

        val request = try {
            msg.toRequest()
        } catch (t: Throwable) {
            logger.error("Failed to parse incoming request", t)
            limiter.release()

            ctx.writeAndFlush(error(t).toNettyResponse())
            return
        }

        val requestId = UUID.randomUUID().toString()
        val contextualRequest = request.withContext(REQUEST_ID_MDC_KEY, requestId)

        val mdcContext = MDCContext(mapOf(
            METHOD_MDC_KEY to request.method.toString(),
            PATH_MDC_KEY to request.uri.path,
        ))

        val serverJob = serverScope.launch(context = mdcContext) {
            callHandler(contextualRequest, ctx)
        }

        cancelJobOnStop(serverJob, ctx)
    }

    private suspend fun callHandler(request: Request, ctx: ChannelHandlerContext) {
        try {
            val result = handler.call(request)

            ctx.writeAndFlush(result.toNettyResponse())
        } catch (c: CancellationException) {
            logger.error("Cancelling server job", c)
            ctx.writeAndFlush(error(c).toNettyResponse())
        } finally {
            limiter.release()
        }
    }

    private fun cancelJobOnStop(handlerJob: Job, ctx: ChannelHandlerContext) {
        ctx.channel()
            .closeFuture()
            .addListener {
                handlerJob.cancel()
            }
    }

}
