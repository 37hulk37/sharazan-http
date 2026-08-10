package com.sharazan.http.handler

import com.sharazan.http.core.Controller
import com.sharazan.http.core.Route
import com.sharazan.http.core.error
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.RequestWithContext
import org.koin.core.Koin
import org.slf4j.LoggerFactory

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

        return route.handler.call(RequestWithContext(request, route.uriTemplate))
    }

}