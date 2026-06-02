package com.sharazan.http.request.routing

import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.routing.Router
import org.http4k.routing.RouterDescription
import org.http4k.routing.RoutingResult
import org.http4k.routing.RoutingResult.Matched
import org.http4k.routing.RoutingResult.NotMatched

class PathRouter(
    val path: Uri
): Router {

    override val description = RouterDescription("path-router")

    override fun invoke(request: Request): RoutingResult {
        if (request.uri.path.contains(path.toString())) {
            return Matched(RouterDescription("path-${request.uri}"))
        }

        return NotMatched(description = RouterDescription("There is no path defined for ${request.uri}"))
    }

}