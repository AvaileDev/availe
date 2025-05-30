package io.availe.config

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

actual val httpClientEngine: HttpClientEngineFactory<*> = JsClient()