package com.sharazan.http.handler

import com.sharazan.core.pipeline.Pipeline
import com.sharazan.http.core.Controller
import com.sharazan.http.core.Route
import com.sharazan.http.core.error
import org.http4k.core.Request
import org.http4k.core.Response

class ServerHandler(
    controllers: List<Controller>,
    pipeline: Pipeline,
): AbstractServerHandler(pipeline) {

    private val routes: List<Route> = controllers
        .flatMap { it.routers }


    override suspend fun handleInternal(request: Request): Response {
        val route = findRoute(request)
            ?: return error("There is no route for ${request.method} ${request.uri.path}")

        return route.handler.handle(request)
    }

    private fun findRoute(request: Request): Route? =
        routes.firstOrNull {
            it.matches(request)
        }

}