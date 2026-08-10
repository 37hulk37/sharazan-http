package com.sharazan.http.handler

import com.sharazan.http.core.Controller
import com.sharazan.http.core.error
import com.sharazan.http.core.Route
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.RequestWithContext
import org.koin.core.Koin
import org.slf4j.LoggerFactory
import kotlin.time.measureTimedValue

class EventLoopHandler(koin: Koin): CoroutineHttpHandler {

    private val logger = LoggerFactory.getLogger(EventLoopHandler::class.java)

    private val routes: List<Route> = koin.getAll<Controller>()
        .flatMap { it.routers }

    override suspend fun call(request: Request): Response {
        val route = routes.firstOrNull { it.matches(request) }

        if (route == null) {
            logger.warn("There is no route for ${request.method} ${request.uri.path}")

            return error("There is no route for ${request.method} ${request.uri.path}")
        }

        val (response, duration) = measureTimedValue {
            route.handler.call(RequestWithContext(request, route.uriTemplate))
        }

        logger.trace(
            "{} {} -> {} ({}ms)",
            request.method,
            request.uri.path,
            response.status,
            duration.inWholeMilliseconds
        )

        return response
    }

}