package com.sharazan.http.request.routing

import com.sharazan.http.request.handler.HttpRequestMethodHandler
import org.http4k.core.*
import org.http4k.routing.and

data class Route(
    val method: Method,
    val fullPath: Uri,
    val handler: HttpRequestMethodHandler,
) {

    fun getRouter() =
        MethodRouter(method)
            .and(PathRouter(fullPath))
            .and(QueryParametersRouter(fullPath.query.toParameters()))
            .and(PathParametersRouter(fullPath.path.toParameters()))

}