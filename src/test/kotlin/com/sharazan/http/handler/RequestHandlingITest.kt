package com.sharazan.http.handler

import com.sharazan.core.exception.ApplicationException
import com.sharazan.core.pipeline.Interceptor
import com.sharazan.core.pipeline.Phase
import com.sharazan.core.pipeline.Pipeline
import com.sharazan.http.core.Controller
import com.sharazan.http.core.Route
import com.sharazan.http.core.get
import com.sharazan.http.core.ok
import com.sharazan.http.interceptor.LoggingInterceptor
import com.sharazan.http.interceptor.RequestContextInterceptor
import com.sharazan.logging.REQUEST_ID_MDC_KEY
import kotlinx.coroutines.runBlocking
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.slf4j.MDC
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RequestHandlingITest {

    @Test
    fun `the request id stamped by RequestContextInterceptor is visible to the controller through MDC`() {
        runBlocking {
            val requestIdSeenByController = mutableListOf<String>()
            val controllers = listOf(controller(get("/hello") {
                requestIdSeenByController += MDC.get(REQUEST_ID_MDC_KEY)
                ok("world")
            }))
            val handler = ServerHandler(controllers, Pipeline(listOf(realHttpPhase())))

            handler.handle(Request(Method.GET, Uri.of("/hello")))

            assertNotNull(requestIdSeenByController.single())
        }
    }

    @Test
    fun `an error thrown by the controller still runs postProcess with the request id from the http phase in MDC`() {
        runBlocking {
            val requestIdSeenByCanary = mutableListOf<String?>()
            val canaryPhase = Phase("canary", listOf(mdcRequestIdCanary(requestIdSeenByCanary)))
            val controllers = listOf(controller(get("/boom") {
                throw ApplicationException("nope", status = Status.UNAUTHORIZED)
            }))
            val handler = ServerHandler(controllers, Pipeline(listOf(realHttpPhase(), canaryPhase)))

            val response = handler.handle(Request(Method.GET, Uri.of("/boom")))

            assertEquals(Status.UNAUTHORIZED, response.status)
            assertNotNull(requestIdSeenByCanary.single())
        }
    }

    @Test
    fun `a phase that fails after the http phase runs postProcess without the request id the http phase already stamped`() = runBlocking {
        val requestIdSeenByCanary = mutableListOf<String?>()
        val failingPhase = Phase("authorization", listOf(
            alwaysThrows(ApplicationException("denied", status = Status.FORBIDDEN))
        ))
        val canaryPhase = Phase("canary", listOf(mdcRequestIdCanary(requestIdSeenByCanary)))
        val handler = ServerHandler(emptyList(), Pipeline(listOf(realHttpPhase(), failingPhase, canaryPhase)))

        val response = handler.handle(Request(Method.GET, Uri.of("/anything")))

        assertEquals(Status.FORBIDDEN, response.status)
        // RequestContextInterceptor already stamped a request id onto an intermediate
        // Request, but ServerHandler never gets to open the MDC context for it -
        // Pipeline.preProcess throws as a whole before returning, so postProcess (and
        // this canary's "after") runs with no request id in MDC at all.
        assertNull(requestIdSeenByCanary.single())
    }

    @Test
    fun `a phase that fails before the http phase never lets it stamp a request id at all`() = runBlocking {
        val requestIdSeenByCanary = mutableListOf<String?>()
        val failingPhase = Phase("authorization", listOf(
            alwaysThrows(ApplicationException("denied", status = Status.FORBIDDEN))
        ))
        val canaryPhase = Phase("canary", listOf(mdcRequestIdCanary(requestIdSeenByCanary)))
        val handler = ServerHandler(emptyList(), Pipeline(listOf(failingPhase, realHttpPhase(), canaryPhase)))

        val response = handler.handle(Request(Method.GET, Uri.of("/anything")))

        assertEquals(Status.FORBIDDEN, response.status)
        // same null result as the previous test, but for a different reason: here the
        // http phase never even got a chance to run, so no id was ever assigned.
        assertNull(requestIdSeenByCanary.single())
    }

    @Test
    fun `a phase that fails skips routing, but ServerHandler still runs postProcess against the error response`() = runBlocking {
        val controllerWasInvoked = AtomicBoolean(false)
        val responsePhaseRan = AtomicBoolean(false)

        val failingPhase = Phase("authorization", listOf(
            alwaysThrows(ApplicationException("denied", status = Status.FORBIDDEN))
        ))
        val canaryPhase = Phase("canary", listOf(interceptor(
            after = { _, response -> responsePhaseRan.set(true); response },
        )))
        val controllers = listOf(controller(get("/hello") {
            controllerWasInvoked.set(true)
            ok("world")
        }))
        val handler = ServerHandler(controllers, Pipeline(listOf(failingPhase, canaryPhase)))

        handler.handle(Request(Method.GET, Uri.of("/hello")))

        // routing/the controller never runs - preProcess never got that far -
        // but ServerHandler guarantees postProcess still runs against the error
        // response it built, so "after" interceptors (e.g. a closing log line)
        // always get a chance to fire, no matter where the pipeline fell apart.
        assertFalse(controllerWasInvoked.get())
        assertTrue(responsePhaseRan.get())
    }

    private fun controller(vararg routes: Route) = object : Controller {
        override val baseUrl = ""
        override val routers = routes.toList()
    }

    private fun realHttpPhase() =
        Phase("http", listOf(RequestContextInterceptor(), LoggingInterceptor()))

    private fun interceptor(
        before: (Request) -> Request = { it },
        after: (Request, Response) -> Response = { _, response -> response },
    ): Interceptor = object : Interceptor {

        override fun before(request: Request): Request = before(request)

        override fun after(request: Request, response: Response): Response = after(request, response)

    }

    private fun alwaysThrows(exception: ApplicationException): Interceptor = interceptor(
        before = { throw exception },
    )

    private fun mdcRequestIdCanary(sink: MutableList<String?>): Interceptor = interceptor(
        after = { _, response -> sink += MDC.get(REQUEST_ID_MDC_KEY); response },
    )

}
