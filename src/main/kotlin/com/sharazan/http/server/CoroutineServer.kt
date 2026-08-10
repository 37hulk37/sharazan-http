package com.sharazan.http.server

import com.sharazan.core.Startable
import com.sharazan.http.handler.CoroutineHttpHandler
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
import org.slf4j.LoggerFactory
import java.io.Closeable

class CoroutineServer(
    private val port: Int,
    private val handlerProvider: () -> CoroutineHttpHandler,
): Http4kServer, Startable, Closeable {

    private val logger = LoggerFactory.getLogger(CoroutineServer::class.java)


    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup(2)

    private val serverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var closeFuture: ChannelFuture? = null


    override fun start(): Http4kServer = apply {
        val handler = handlerProvider()

        val channel = getBootstrap(handler)
            .bind(port)
            .sync()
            .channel()

        closeFuture = channel?.closeFuture()

        logger.info("Server started on port $port")
    }

    override fun stop(): Http4kServer = apply {
        serverScope.cancel()

        workerGroup.shutdownGracefully().sync()
        bossGroup.shutdownGracefully().sync()

        logger.info("Server stopped")
    }

    override fun port(): Int = port

    override fun started() {
        start()
    }

    override fun close() {
        stop()
    }

    private fun getBootstrap(handler: CoroutineHttpHandler): ServerBootstrap {
        val bootstrap = ServerBootstrap()

        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(CoroutineChannelInitializer(handler, serverScope))
            .option(ChannelOption.SO_BACKLOG, 1000)
            .childOption(ChannelOption.SO_KEEPALIVE, true)

        return bootstrap
    }

}