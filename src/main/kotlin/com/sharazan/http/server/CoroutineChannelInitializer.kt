package com.sharazan.http.server

import com.sharazan.core.Handler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpServerKeepAliveHandler
import io.netty.handler.stream.ChunkedWriteHandler
import kotlinx.coroutines.CoroutineScope

class CoroutineChannelInitializer(
    private val httpHandler: Handler,
    private val serverScope: CoroutineScope
): ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline()
            .addLast("codec", HttpServerCodec())

        ch.pipeline()
            .addLast("keepAlive", HttpServerKeepAliveHandler())

        ch.pipeline()
            .addLast("aggregator", HttpObjectAggregator(Int.MAX_VALUE))

        ch.pipeline()
            .addLast("streamer", ChunkedWriteHandler())

        ch.pipeline()
            .addLast("http", CoroutineChannelHandler(httpHandler, serverScope))
    }

}