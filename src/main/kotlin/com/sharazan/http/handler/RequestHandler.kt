package com.sharazan.http.handler

import com.sharazan.core.Handler
import org.http4k.core.Request
import org.http4k.core.Response

class RequestHandler(
    private val action: suspend (Request) -> Response,
): Handler {

    override suspend fun handle(request: Request): Response
        = action(request)

}