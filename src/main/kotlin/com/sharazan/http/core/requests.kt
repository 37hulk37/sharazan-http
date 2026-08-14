package com.sharazan.http.core

import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.format.Jackson.auto

inline fun <reified T : Any> Request.getBody(): T =
    Body.auto<T>().toLens()(this)
