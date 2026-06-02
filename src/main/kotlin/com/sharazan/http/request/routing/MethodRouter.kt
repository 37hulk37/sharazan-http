package com.sharazan.http.request.routing

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.routing.Router
import org.http4k.routing.RouterDescription
import org.http4k.routing.RoutingResult
import org.http4k.routing.RoutingResult.Matched
import org.http4k.routing.RoutingResult.NotMatched

class MethodRouter(
    val method: Method
): Router {

    override val description = RouterDescription("method-router")

    override fun invoke(request: Request): RoutingResult {
        if (request.method == method) {
            return Matched(RouterDescription("method-${request.method}"))
        }

        return NotMatched(description = RouterDescription("There is no method defined for ${request.method}"))
    }

}