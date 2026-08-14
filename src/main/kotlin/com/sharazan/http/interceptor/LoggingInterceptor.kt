package com.sharazan.http.interceptor

import com.sharazan.core.pipeline.Interceptor
import org.http4k.core.Request
import org.http4k.core.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LoggingInterceptor: Interceptor {

    private val logger: Logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override fun before(request: Request): Request {
        logger.trace("Started processing request")

        return request
    }


    override fun after(request: Request, response: Response): Response {
        logger.trace("Request has processed")

        return response
    }
}