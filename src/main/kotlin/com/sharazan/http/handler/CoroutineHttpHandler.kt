package com.sharazan.http.handler

import org.http4k.core.Request
import org.http4k.core.Response

interface CoroutineHttpHandler {

    suspend fun call(request: Request): Response

}