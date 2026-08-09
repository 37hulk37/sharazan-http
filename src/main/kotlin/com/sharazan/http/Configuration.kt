package com.sharazan.http

import kotlinx.serialization.Serializable

@Serializable
data class Configuration(
    val host: String = "127.0.0.1",
    val port: Int = 8080,
)