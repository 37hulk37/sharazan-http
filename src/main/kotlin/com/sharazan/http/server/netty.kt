package com.sharazan.http.server

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import org.http4k.core.*
import org.http4k.server.supportedOrNull
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

internal fun FullHttpRequest.toRequest(): Request {
    val httpMethod = Method.supportedOrNull(method().name())
        ?: throw RuntimeException("there is no http method like ${method().name()}")

    return MemoryRequest(
        httpMethod,
        Uri.of(uri()),
        headers().map { it.key to it.value },
        Body(ByteArrayInputStream(ByteBufUtil.getBytes(content()))),
    )
}

internal fun Response.toNettyResponse(): FullHttpResponse {
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
