package com.sharazan.http.core

import org.http4k.core.UriTemplate

open class EndpointRegistry(
    controllers: Collection<Controller>,
) {

    protected val routesByPath = controllers.flatMap { it.routers }

    fun findRoute(path: UriTemplate) =
        routesByPath.firstOrNull {
            route ->  path.matches(route.fullPath.path)
        }
            ?: throw RuntimeException("Route not found: $path")

}