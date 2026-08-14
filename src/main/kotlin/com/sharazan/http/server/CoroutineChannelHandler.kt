package com.sharazan.http.server

import com.sharazan.core.Handler
import com.sharazan.core.pipeline.Pipeline
import com.sharazan.core.withContext
import com.sharazan.http.core.error
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import org.http4k.core.*
import org.slf4j.LoggerFactory
import java.util.UUID

class CoroutineChannelHandler(
    private val handler: Handler,
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

        val request = getRequest(msg, ctx)
            ?: throw RuntimeException("request is null")

        val serverJob = serverScope.launch {
            execute(request, ctx)
        }

        cancelJobOnStop(serverJob, ctx)
    }

    private fun getRequest(msg: FullHttpRequest, ctx: ChannelHandlerContext): Request? =
        try {
            msg.toRequest()
        } catch (t: Throwable) {
            logger.error("Failed to parse incoming request", t)
            limiter.release()

            ctx.writeAndFlush(error(t).toNettyResponse())

            null
        }

    private suspend fun execute(request: Request, ctx: ChannelHandlerContext) {
        try {
            val result = handler.handle(request)

            ctx.writeAndFlush(result.toNettyResponse())
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            logger.error("Cancelling server job", t)
            ctx.writeAndFlush(error(t).toNettyResponse())
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
