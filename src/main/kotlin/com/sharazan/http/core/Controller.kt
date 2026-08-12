package com.sharazan.http.core

interface Controller {

    val baseUrl: String

    val routers: List<Route>

}