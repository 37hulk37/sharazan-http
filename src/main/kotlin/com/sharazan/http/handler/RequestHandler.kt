package com.sharazan.http.handler

import com.sharazan.core.Handler
import com.sharazan.http.core.error
import kotlinx.coroutines.CancellationException
import org.http4k.core.Request
import org.http4k.core.Response

class RequestHandler(
    private val action: suspend (Request) -> Response,
): Handler {

    override suspend fun handle(request: Request): Response
        = try {
            action(request)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            error(t)
        }

}