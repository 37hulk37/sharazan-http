package com.sharazan.http.server

import com.sharazan.http.core.error
import com.sharazan.http.request.handler.CoroutineHttpHandler
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import org.http4k.core.*
import org.http4k.server.supportedOrNull

class CoroutineChannelHandler(
    private val handler: CoroutineHttpHandler,
    private val serverScope: CoroutineScope,
): SimpleChannelInboundHandler<FullHttpRequest>() {

    private val limiter = Semaphore(100)

    override fun channelRead0(
        ctx: ChannelHandlerContext,
        msg: FullHttpRequest
    ) {
        if (!limiter.tryAcquire()) {
            ctx.writeAndFlush(error("Too many requests"))
            return
        }

        val serverJob = serverScope.launch {
            try {
                val response = handler.call(toRequest(msg))
                ctx.writeAndFlush(response)
            } finally {
                limiter.release()
            }
        }

        cancelJobOnStop(serverJob, ctx)
    }

    private fun cancelJobOnStop(handlerJob: Job, ctx: ChannelHandlerContext) {
        ctx.channel()
            .closeFuture()
            .addListener {
                handlerJob.cancel()
            }
    }

    private fun toRequest(msg: FullHttpRequest): Request
        = Method.supportedOrNull(msg.method().name())
            ?.let { method ->
                MemoryRequest(
                    method,
                    Uri.of(msg.uri()),
                    msg.headers().map { it.key to it.value },
                    Body(ByteBufInputStream(msg.content())),
                )
            }!!

}