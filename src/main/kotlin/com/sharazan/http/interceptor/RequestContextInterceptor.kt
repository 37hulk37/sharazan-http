package com.sharazan.http.interceptor

import com.sharazan.core.pipeline.Interceptor
import com.sharazan.core.withContext
import com.sharazan.logging.REQUEST_ID_MDC_KEY
import org.http4k.core.Request
import java.util.*

class RequestContextInterceptor: Interceptor {

    override fun before(request: Request): Request {
        val requestId = UUID.randomUUID().toString()

        return request.withContext(REQUEST_ID_MDC_KEY, requestId)
    }

}