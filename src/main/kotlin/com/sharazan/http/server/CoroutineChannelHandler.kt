package com.sharazan.http.server

import com.sharazan.http.core.error
import com.sharazan.http.handler.CoroutineHttpHandler
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import org.http4k.core.*
import org.http4k.routing.RequestWithContext
import org.http4k.server.supportedOrNull
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

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
            ctx.writeAndFlush(error("Too many requests").toNettyResponse())
            return
        }

        val request = msg.toRequest()

        val serverJob = serverScope.launch {
            try {
                val response = try {
                    handler.call(request)
                } catch (c: CancellationException) {
                    throw c
                } catch (t: Throwable) {
                    error(t)
                }

                ctx.writeAndFlush(response.toNettyResponse())
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

}

private fun FullHttpRequest.toRequest(): Request {
    val httpMethod = Method.supportedOrNull(method().name())
        ?: throw RuntimeException("there is no http method like ${method().name()}")

    val delegate = MemoryRequest(
        httpMethod,
        Uri.of(uri()),
        headers().map { it.key to it.value },
        Body(ByteArrayInputStream(ByteBufUtil.getBytes(content()))),
    )

    return RequestWithContext(delegate, emptyMap())
}

private fun Response.toNettyResponse(): FullHttpResponse {
    val content = Unpooled.copiedBuffer(bodyString(), StandardCharsets.UTF_8)

    val nettyResponse = DefaultFullHttpResponse(
        HttpVersion.HTTP_1_1,
        HttpResponseStatus.valueOf(status.code, status.description),
        content,
    )

    headers.forEach { (name, value) -> value?.let { nettyResponse.headers().add(name, it) } }
    nettyResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes())

    return nettyResponse
}