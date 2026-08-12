package com.sharazan.http.configuration

import kotlinx.serialization.Serializable

@Serializable
data class HttpProperties(
    val host: String = "127.0.0.1",
    val port: Int = 8080,
)