package com.sharazan.http.core

import org.http4k.core.ContentType
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson
import org.http4k.format.Jackson.json
import org.http4k.lens.contentType

fun <T: Any> ok(body: T): Response
    = Response(Status.OK)
        .contentType(ContentType.APPLICATION_JSON)
        .body(Jackson.asFormatString(body))

fun <T: Any> okWithPage(
     values: Collection<T>,
     page: Int = 0,
     size: Int = values.size,
     totalElements: Long = values.size.toLong(),
): Response
    = Response(Status.OK)
        .contentType(ContentType.APPLICATION_JSON)
        .body(Jackson.asFormatString(Page( page, size, totalElements, values)))

fun ok(): Response
    = Response(Status.OK)

fun error(t: Throwable): Response
    = Response(Status.BAD_REQUEST)
        .contentType(ContentType.APPLICATION_JSON)
        .body(Jackson.asFormatString(t))

fun error(cause: String): Response
    = Response(Status.BAD_REQUEST)
        .contentType(ContentType.APPLICATION_JSON)
        .body(Jackson.asFormatString(cause))

fun error(): Response
    = Response(Status.BAD_REQUEST)