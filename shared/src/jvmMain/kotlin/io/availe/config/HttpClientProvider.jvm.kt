package io.availe.config

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual val httpClientEngine: HttpClientEngineFactory<*> = CIO