package com.sharazan.http.core

import com.sharazan.http.request.handler.HttpRequestMethodHandler
import org.http4k.core.Request
import org.http4k.core.Response

fun handler(
    action: suspend (Request) -> Response,
) = HttpRequestMethodHandler(action)