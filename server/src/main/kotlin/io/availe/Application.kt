package io.availe

import io.availe.client.NLIPClient
import io.availe.client.OllamaClient
import io.availe.config.ClientProvider
import io.availe.routes.NLIPRoutes
import io.availe.routes.healthRoutes
import io.availe.routes.staticRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*


fun main() {
    embeddedServer(CIO, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    install(ContentNegotiation) {
        json()
    }

    val httpClient = ClientProvider.client


    val internalChat = OllamaClient(httpClient)
    val externalChat = NLIPClient(httpClient, Url("http://localhost:8004"))

    routing {
        staticRoutes()
        healthRoutes(externalChat)
        NLIPRoutes(internalChat, httpClient)
    }
}
