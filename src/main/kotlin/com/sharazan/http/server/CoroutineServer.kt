package com.sharazan.http.server

import com.sharazan.core.Startable
import com.sharazan.http.request.handler.AppHttpHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.http4k.server.Http4kServer
import java.io.Closeable
import java.util.concurrent.TimeUnit

class CoroutineServer(
    private val port: Int = 8080,
    private val handler: AppHttpHandler
): Http4kServer, Startable, Closeable {

    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup(2)

    private val serverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var closeFuture: ChannelFuture? = null

    override fun start(): Http4kServer = apply {
        val channel = getBootstrap()
            .bind(port)
            .sync()
            .channel()

        closeFuture = channel?.closeFuture()
    }

    override fun stop(): Http4kServer = apply {
        serverScope.cancel()

        workerGroup.shutdownGracefully(
            100000L,
            100000L,
            TimeUnit.MILLISECONDS
        ).sync()

        bossGroup.shutdownGracefully(
            100000L / 2,
            100000L,
            TimeUnit.MILLISECONDS
        ).sync()
    }

    override fun port(): Int = port

    override fun started() {
        start()
    }

    override fun close() {
        stop()
    }

    private fun getBootstrap(): ServerBootstrap {
        val bootstrap = ServerBootstrap()

        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(CoroutineChannelInitializer(handler, serverScope))
            .option(ChannelOption.SO_BACKLOG, 1000)
            .childOption(ChannelOption.SO_KEEPALIVE, true)

        return bootstrap
    }

}