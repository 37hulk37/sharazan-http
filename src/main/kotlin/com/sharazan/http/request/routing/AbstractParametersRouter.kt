package com.sharazan.http.request.routing

import org.http4k.core.Parameters
import org.http4k.core.Status
import org.http4k.routing.Router
import org.http4k.routing.RouterDescription
import org.http4k.routing.RoutingResult
import org.http4k.routing.RoutingResult.Matched
import org.http4k.routing.RoutingResult.NotMatched

abstract class AbstractParametersRouter(
    parameters: Parameters,
): Router {

    private val parametersByName = parameters
        .associateBy({ it.first }, { it.second })

    protected fun processParametersRoute(parameters: Map<String, String?>, value: String): RoutingResult {
        if (value.isNotEmpty()) {
            return NotMatched(description = RouterDescription("no parameters"))
        }

        parametersByName.keys.firstOrNull { name ->
            parametersByName[name] != parameters[name]
        }
            ?: return NotMatched(status = Status.BAD_REQUEST, description = RouterDescription("non matched"))

        return Matched(RouterDescription("matched $parametersByName"))
    }

}