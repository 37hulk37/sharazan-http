package com.sharazan.http.request.handler

import com.sharazan.http.core.error
import com.sharazan.http.core.ok
import org.http4k.core.Request
import org.http4k.core.Response

class HttpRequestMethodHandler(
    private val action: suspend (Request) -> Response,
): CoroutineHttpHandler {

    override suspend fun call(request: Request)
        = try {
            ok(action.invoke(request))
        } catch (t: Throwable) {
            error(t)
        }

}