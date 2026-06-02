package com.sharazan.http.core

import com.sharazan.http.request.handler.HttpRequestMethodHandler
import com.sharazan.http.request.routing.Route
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Uri

fun createRoutes(vararg routes: Route) = routes.toList()

fun post(baseUrl: String, path: String? = null, handler: suspend (Request) -> Response)
    = Route(Method.POST, Uri.of(baseUrl + path), handler = HttpRequestMethodHandler(handler))

fun put(baseUrl: String, path: String, handler: suspend (Request) -> Response)
    = Route(Method.PUT, Uri.of(baseUrl + path), handler = HttpRequestMethodHandler(handler))

fun get(baseUrl: String, path: String? = null, handler: suspend (Request) -> Response)
    = Route(Method.GET, Uri.of(baseUrl + path), handler = HttpRequestMethodHandler(handler))

fun delete(baseUrl: String, path: String, handler: suspend (Request) -> Response)
    = Route(Method.DELETE, Uri.of(baseUrl + path), handler = HttpRequestMethodHandler(handler))