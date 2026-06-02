package com.sharazan.http.core

import org.http4k.core.Request
import org.http4k.format.Jackson.json
import org.http4k.routing.path

inline fun <reified T: Any> Request.getBody(): T
    = this.json(body.text)
        .json<T>()

inline fun <reified T: Any> Request.getPathParam(name: String): T? {
    val param = this.path(name)
        ?: return null

    return param as T
}

inline fun <reified T: Any> Request.getQueryParam(name: String): T? {
    val queryParam = this.query(name)
        ?: return null

    return queryParam as T
}
