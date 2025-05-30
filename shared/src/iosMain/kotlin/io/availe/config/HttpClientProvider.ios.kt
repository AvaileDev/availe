package io.availe.config

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual val httpClientEngine: HttpClientEngineFactory<*> = Darwin