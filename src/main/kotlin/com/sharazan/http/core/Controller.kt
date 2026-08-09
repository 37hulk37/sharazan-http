package com.sharazan.http.core

import org.koin.core.component.KoinComponent

interface Controller: KoinComponent {

    val baseUrl: String

    val routers: List<Route>

}