package com.sharazan.http.error

import org.http4k.core.Status

open class ApiException(
    message: String,
    val status: Status = Status.NOT_FOUND,
) : RuntimeException(message)