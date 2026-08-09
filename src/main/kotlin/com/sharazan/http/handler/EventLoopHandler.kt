package com.sharazan.http.handler

import com.sharazan.http.core.Controller
import com.sharazan.http.core.error
import com.sharazan.http.core.Route
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.RequestWithContext
import org.koin.core.Koin

class EventLoopHandler(koin: Koin): CoroutineHttpHandler {

    private val routes: List<Route> = koin.getAll<Controller>()
        .flatMap { it.routers }

    override suspend fun call(request: Request): Response {
        val route = routes.firstOrNull { it.matches(request) }
            ?: return error("There is no route for ${request.method} ${request.uri.path}")

        return route.handler.call(RequestWithContext(request, route.uriTemplate))
    }

}