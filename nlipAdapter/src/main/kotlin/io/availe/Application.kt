package io.availe

import io.availe.config.ClientProvider
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*


fun main() {
    embeddedServer(CIO, port = SERVER_PORT + 1, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val httpClient = ClientProvider.client

}