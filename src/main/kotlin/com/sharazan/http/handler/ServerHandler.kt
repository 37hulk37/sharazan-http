package com.sharazan.http.handler

import com.sharazan.core.Handler
import com.sharazan.core.pipeline.Pipeline
import com.sharazan.http.core.Controller
import com.sharazan.http.core.Route
import com.sharazan.http.core.error
import org.http4k.core.Request
import org.http4k.core.Response
import org.slf4j.LoggerFactory

class ServerHandler(
    controllers: List<Controller>,
    private val pipeline: Pipeline,
): Handler {

    private val routes: List<Route> = controllers
        .flatMap { it.routers }


    private val logger = LoggerFactory.getLogger(ServerHandler::class.java)


    override suspend fun handle(request: Request): Response {
        logger.debug("Handling route: {}", request)

        val processedRequest = pipeline.preProcess(request)

        val response = handleRequest(processedRequest)

        return pipeline.postProcess(processedRequest, response)
    }

    private suspend fun handleRequest(request: Request): Response {
        val route = routes.firstOrNull {
            it.matches(request)
        }
        if (route == null) {
            logger.warn("There is no route for ${request.method} ${request.uri.path}")

            return error("There is no route for ${request.method} ${request.uri.path}")
        }

        return route.handler.handle(request)
    }

}