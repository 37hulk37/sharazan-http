package com.sharazan.http.interceptor

import com.sharazan.core.getContext
import com.sharazan.core.getContextOrNull
import com.sharazan.core.pipeline.Interceptor
import com.sharazan.logging.REQUEST_ID_MDC_KEY
import org.http4k.core.Request
import org.http4k.core.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LoggingInterceptor: Interceptor {

    private val logger: Logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override fun before(request: Request): Request {
        val requestId = request.getContext<String>(REQUEST_ID_MDC_KEY)
        logger.trace("Started processing request {} with id {}", request, requestId)

        return request
    }


    override fun after(request: Request, response: Response): Response {
        val requestId = request.getContextOrNull<String>(REQUEST_ID_MDC_KEY)
        logger.trace("Request {} with id {} has processed", request, requestId)

        return response
    }
}