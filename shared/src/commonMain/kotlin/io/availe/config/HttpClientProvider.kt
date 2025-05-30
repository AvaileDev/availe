package io.availe.config

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

expect val httpClientEngine: HttpClientEngineFactory<*>

object HttpClientProvider {
    val client = HttpClient(httpClientEngine) {
        install(ContentNegotiation) {
            json()
        }
    }
}
