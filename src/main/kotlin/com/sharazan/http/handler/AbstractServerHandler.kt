package com.sharazan.http.handler

import com.sharazan.core.Handler
import com.sharazan.core.exception.ApplicationException
import com.sharazan.core.getContextOrNull
import com.sharazan.core.pipeline.Pipeline
import com.sharazan.http.core.error
import com.sharazan.logging.METHOD_MDC_KEY
import com.sharazan.logging.PATH_MDC_KEY
import com.sharazan.logging.REQUEST_ID_MDC_KEY
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.http4k.core.Request
import org.http4k.core.Response
import org.slf4j.LoggerFactory

abstract class AbstractServerHandler(
    private val pipeline: Pipeline,
): Handler {

    private val logger = LoggerFactory.getLogger(AbstractServerHandler::class.java)


    override suspend fun handle(request: Request): Response {
        return try {
            val processed = pipeline.preProcess(request)

            withContext(getMdcCtx(processed)) {
                val response = handleSafely(processed)

                pipeline.postProcess(processed, response)
            }
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            logger.warn("Failed pre-process request", t)

            val response = handleError(t)
            pipeline.postProcess(request, response)
        }
    }


    protected abstract suspend fun handleInternal(request: Request): Response


    private fun getMdcCtx(request: Request): MDCContext {
        val requestId = request.getContextOrNull<String>("requestId")

        val context = buildMap {
            requestId?.let { put(REQUEST_ID_MDC_KEY, it) }
            put(METHOD_MDC_KEY, request.method.name)
            put(PATH_MDC_KEY, request.uri.path)
        }

        return MDCContext(context)
    }

    private suspend fun handleSafely(request: Request): Response = try {
        handleInternal(request)
    } catch (c: CancellationException) {
        throw c
    } catch (t: Throwable) {
        logger.warn("Failed handle request", t)

        handleError(t)
    }

    private fun handleError(t: Throwable): Response {
        val ae = t as? ApplicationException ?: t

        return error(ae.message ?: "Something went wrong", ae)
    }

}