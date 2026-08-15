package com.sharazan.http.configuration

import com.sharazan.core.AppBuilder
import com.sharazan.core.Handler
import com.sharazan.core.Lifecycle
import com.sharazan.core.pipeline.Interceptor
import com.sharazan.core.pipeline.Phase
import com.sharazan.core.source.ConfigurationSource
import com.sharazan.core.source.get
import com.sharazan.http.core.Controller
import com.sharazan.http.core.EndpointRegistry
import com.sharazan.http.handler.ServerHandler
import com.sharazan.http.interceptor.LoggingInterceptor
import com.sharazan.http.interceptor.RequestContextInterceptor
import com.sharazan.http.server.CoroutineServer
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

fun AppBuilder.http(block: HttpProperties.() -> Unit) = registerHttp { HttpProperties().apply(block) }

fun AppBuilder.http() = registerHttp {
    get<ConfigurationSource>().get<HttpProperties>("sharazan.http")
}

private fun AppBuilder.registerHttp(props: Scope.() -> HttpProperties) = apply {
    val httpModule = module {
        single {
            LoggingInterceptor()
        } bind Interceptor::class

        single { EndpointRegistry(getAll<Controller>()) }

        single {
            RequestContextInterceptor()
        } bind Interceptor::class

        single(named("http")) {
            Phase("http", listOf(
                get<RequestContextInterceptor>(),
                get<LoggingInterceptor>(),
            ))
        }

        single { props() }

        single {
            ServerHandler(getAll<Controller>(), get())
        } bind Handler::class

        single {
            CoroutineServer(get<HttpProperties>().port, get<ServerHandler>())
        } bind Lifecycle::class
    }

    addModule(httpModule)
}