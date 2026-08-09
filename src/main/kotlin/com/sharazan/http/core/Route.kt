package com.sharazan.http.core

import com.sharazan.http.handler.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.core.UriTemplate

data class Route(
    val method: Method,
    val fullPath: Uri,
    val handler: HttpHandler,
) {

    val uriTemplate: UriTemplate = UriTemplate.from(fullPath.path)

    fun matches(request: Request): Boolean =
        request.method == method && uriTemplate.matches(request.uri.path)

}