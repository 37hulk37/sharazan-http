package com.sharazan.http.request.handler

import com.sharazan.http.controller.Controller
import com.sharazan.http.core.error
import com.sharazan.http.request.routing.Route
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.RoutingResult
import org.koin.core.component.KoinComponent

class AppHttpHandler: CoroutineHttpHandler, KoinComponent {

    private val routesByPath = getKoin().getAll<Controller>()
        .flatMap { it.routers }
        .associateBy { it.fullPath.path }

    override suspend fun call(request: Request): Response {
        val route = routesByPath[request.uri.path]
            ?: return error("There is no ${request.uri.path} in routes")

        if (filter(route, request) is RoutingResult.NotMatched) {
            return error("There is no route with path ${request.uri.path}")
        }

        return route.handler.call(request)
    }

    private fun filter(route: Route, request: Request): RoutingResult {
        val router  = route.getRouter()

        return router.invoke(request)
    }

}