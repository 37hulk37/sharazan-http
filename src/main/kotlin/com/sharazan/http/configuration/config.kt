package com.sharazan.http.configuration

import com.sharazan.core.AppBuilder
import com.sharazan.core.properties.ConfigurationSource
import com.sharazan.http.Configuration
import com.sharazan.http.handler.EventLoopHandler
import com.sharazan.http.server.CoroutineServer
import org.koin.core.Koin

fun AppBuilder.http(port: Int? = null) = apply {
    val configuration = get<ConfigurationSource>()
        ?.get<Configuration>("sharazan.http")
        ?: Configuration()

    val resolvedPort = port ?: configuration.port

    val server = CoroutineServer(resolvedPort) {
        val koin = checkNotNull(get<Koin>()) {
            "Koin is not installed on this AppBuilder"
        }

        EventLoopHandler(koin)
    }

    this.install(server)
}