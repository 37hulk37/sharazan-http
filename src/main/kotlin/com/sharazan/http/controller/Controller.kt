package com.sharazan.http.controller

import com.sharazan.http.request.routing.Route
import org.koin.core.component.KoinComponent

interface Controller: KoinComponent {

    val baseUrl: String

    val routers: List<Route>

}