package com.sharazan.http.handler

import com.sharazan.core.exception.ApplicationException
import com.sharazan.core.pipeline.Interceptor
import com.sharazan.core.pipeline.Phase
import com.sharazan.core.pipeline.Pipeline
import com.sharazan.http.core.Controller
import com.sharazan.http.core.Route
import com.sharazan.http.core.get
import com.sharazan.http.core.ok
import kotlinx.coroutines.runBlocking
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.format.Jackson
import kotlin.test.Test
import kotlin.test.assertEquals


class ServerHandlerTest {

    @Test
    fun `routes a matching request to its controller`() = runBlocking {
        val controllers = listOf(controller(get("/hello") {
            ok("world")
        }))
        val handler = ServerHandler(controllers, Pipeline(emptyList()))


        val response = handler.handle(Request(Method.GET, Uri.of("/hello")))
        assertEquals(Status.OK, response.status)
        assertEquals("world", Jackson.asA<String>(response.bodyString()))

    }

    @Test
    fun `returns an error response when no route matches`() = runBlocking {
        val handler = ServerHandler(emptyList(), Pipeline(emptyList()))
        val response = handler.handle(Request(Method.GET, Uri.of("/missing")))

        assertEquals(Status.BAD_REQUEST, response.status)
    }

    @Test
    fun `converts an exception thrown by a controller action into an error response`() = runBlocking {
        val controllers = listOf(controller(get("/boom") {
            throw ApplicationException("nope", status = Status.UNAUTHORIZED)
        }))
        val handler = ServerHandler(controllers, Pipeline(emptyList()))


        val response = handler.handle(Request(Method.GET, Uri.of("/boom")))
        assertEquals(Status.UNAUTHORIZED, response.status)
    }

    @Test
    fun `converts an exception thrown during pipeline preProcess into an error response too`() = runBlocking {
        val phase = Phase("test", listOf(
            throwingInterceptor(ApplicationException("denied", status = Status.FORBIDDEN))
        ))
        val handler = ServerHandler(emptyList(), Pipeline(listOf(phase)))

        val response = handler.handle(Request(Method.GET, Uri.of("/anything")))

        assertEquals(Status.FORBIDDEN, response.status)
    }

    private fun controller(vararg routes: Route) = object : Controller {
        override val baseUrl = ""
        override val routers = routes.toList()
    }

    private fun throwingInterceptor(exception: ApplicationException) = object : Interceptor {

        override fun before(request: Request) = throw exception

    }

}
