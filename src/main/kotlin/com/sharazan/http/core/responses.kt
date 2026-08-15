package com.sharazan.http.core

import com.sharazan.core.exception.handleException
import com.sharazan.http.Page
import org.http4k.core.ContentType
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson
import org.http4k.lens.contentType

fun <T: Any> ok(body: T): Response
    = response(body)


fun <T: Any> ok(
     values: Collection<T>,
     page: Int = 0,
     size: Int = values.size,
     totalElements: Long = values.size.toLong(),
): Response = response(Page(page, size, totalElements, values))


fun ok(): Response
    = Response(Status.OK)


fun error(
    message: String,
    t: Throwable? = null
): Response {
    val response = handleException(message, t)

    return response(response, Status(response.status, ""))
}

private fun <T: Any> response(body: T, status: Status = Status.OK): Response
    = Response(status)
        .contentType(ContentType.APPLICATION_JSON)
        .body(Jackson.asFormatString(body))