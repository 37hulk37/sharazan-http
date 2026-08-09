package com.sharazan.http.handler

import com.sharazan.http.core.error
import kotlinx.coroutines.CancellationException
import org.http4k.core.Request
import org.http4k.core.Response

class HttpHandler(
    private val action: suspend (Request) -> Response,
): CoroutineHttpHandler {

    override suspend fun call(request: Request)
        = try {
            action(request)
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            error(t)
        }

}