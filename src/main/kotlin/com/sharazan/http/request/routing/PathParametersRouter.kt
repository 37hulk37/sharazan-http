package com.sharazan.http.request.routing

import org.http4k.core.Parameters
import org.http4k.core.Request
import org.http4k.core.toParameters
import org.http4k.routing.RouterDescription

class PathParametersRouter(
    parameters: Parameters
): AbstractParametersRouter(parameters) {

    override val description = RouterDescription("path-parameters-router")

    override fun invoke(request: Request) = processParametersRoute(
            getPathParams(request),
            request.uri.path
    )

    private fun getPathParams(request: Request): Map<String, String?> {
        if (request.uri.path.isEmpty()) {
            return emptyMap()
        }
        return request.uri.path.toParameters()
            .associateBy({ it.first }, { it.second })
    }

}