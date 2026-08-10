package com.sharazan.http.core

import org.http4k.core.Request
import org.http4k.format.Jackson.json

fun <T> Request.getBody(): T
    = this.json()
