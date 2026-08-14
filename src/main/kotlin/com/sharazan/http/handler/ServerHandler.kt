package com.sharazan.http.handler

import com.sharazan.core.Handler
import com.sharazan.core.getContext
import com.sharazan.core.getContextOrNull
import com.sharazan.core.pipeline.Pipeline
import com.sharazan.http.core.Controller
import com.sharazan.http.core.Route
import com.sharazan.http.core.error
import com.sharazan.logging.METHOD_MDC_KEY
import com.sharazan.logging.PATH_MDC_KEY
import com.sharazan.logging.REQUEST_ID_MDC_KEY
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.routing.path
import org.slf4j.LoggerFactory
import org.slf4j.MDC

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

        return withContext(getMdcCtx(processedRequest)) {
            val response = handleRequest(processedRequest)

            pipeline.postProcess(processedRequest, response)
        }
    }

    private fun getMdcCtx(request: Request): MDCContext {
        val requestId = request.getContext<String>("requestId")

        return MDCContext(mapOf(
            REQUEST_ID_MDC_KEY to requestId,
            METHOD_MDC_KEY to request.method.name,
            PATH_MDC_KEY to request.uri.path,
        ))
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