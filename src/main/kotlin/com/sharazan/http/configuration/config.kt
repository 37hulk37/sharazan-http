package com.sharazan.http.configuration

import com.sharazan.core.AppBuilder
import com.sharazan.core.Handler
import com.sharazan.core.pipeline.Interceptor
import com.sharazan.core.pipeline.Phase
import com.sharazan.core.properties.ConfigurationSource
import com.sharazan.http.core.Controller
import com.sharazan.http.core.EndpointRegistry
import com.sharazan.http.handler.ServerHandler
import com.sharazan.http.interceptor.LoggingInterceptor
import com.sharazan.http.interceptor.RequestContextInterceptor
import com.sharazan.http.server.CoroutineServer
import org.koin.dsl.bind
import org.koin.dsl.module

fun AppBuilder.http(block: HttpProperties.() -> Unit) = apply {
    val props = HttpProperties().apply(block)

    val httpModule = module {
        single {
            LoggingInterceptor()
        } bind Interceptor::class

        single { EndpointRegistry(getAll<Controller>()) }

        single {
            RequestContextInterceptor()
        } bind Interceptor::class

        single {
            Phase("http", listOf(
                get<LoggingInterceptor>(),
                get<RequestContextInterceptor>()
            ))
        }
        single {
            props
        }
        single {
            ServerHandler(getAll<Controller>(), get())
        } bind Handler::class

        single {
            CoroutineServer(props.port, get<ServerHandler>())
        }
    }

    addModule(httpModule)
}

fun AppBuilder.http() = apply {
    val httpModule = module {
        single {
            LoggingInterceptor()
        } bind Interceptor::class

        single { EndpointRegistry(getAll<Controller>()) }

        single {
            RequestContextInterceptor()
        } bind Interceptor::class

        single {
            Phase("http", listOf(
                get<LoggingInterceptor>(),
                get<RequestContextInterceptor>()
            ))
        }
        single {
            ServerHandler(getAll<Controller>(), get())
        } bind Handler::class

        single {
            val source = get<ConfigurationSource>()

            source.get<HttpProperties>("http")
        }

        single {
            val props = get<HttpProperties>()

            CoroutineServer(props.port, get<ServerHandler>())
        }
    }

    addModule(httpModule)
}