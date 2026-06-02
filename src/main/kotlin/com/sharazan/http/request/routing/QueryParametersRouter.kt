package com.sharazan.http.request.routing

import org.http4k.core.Parameters
import org.http4k.core.Request
import org.http4k.core.toParameters
import org.http4k.routing.RouterDescription

class QueryParametersRouter(
    parameters: Parameters
): AbstractParametersRouter(parameters) {

    override val description = RouterDescription("query-parameters-router")

    override fun invoke(request: Request) = processParametersRoute(
        getQueryParams(request),
        request.uri.query
    )

    private fun getQueryParams(request: Request): Map<String, String?> {
        if (request.uri.query.isEmpty()) {
            return emptyMap()
        }
        return request.uri.query.toParameters()
            .associateBy({ it.first }, { it.second })
    }
}