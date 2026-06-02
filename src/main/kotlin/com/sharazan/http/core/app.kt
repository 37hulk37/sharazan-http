package com.sharazan.http.core

import com.sharazan.core.AppBuilder
import com.sharazan.http.request.handler.AppHttpHandler
import com.sharazan.http.server.CoroutineServer

fun AppBuilder.http(port: Int) = apply {
    val appHandler = AppHttpHandler()

    val server = CoroutineServer(port, appHandler)

    this.install(server)

    server.start()
}