package io.availe

import io.availe.client.OllamaClient
import io.ktor.client.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(CIO, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)

}

fun Application.module() {
    val httpClient = HttpClient(io.ktor.client.engine.cio.CIO) {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json()
        }
    }

    val ollamaClient = OllamaClient(httpClient)

    routing {

        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        get("/ask") {
            val prompt = "Hello world"
            val response = ollamaClient.generate(prompt)
            call.respondText(response)
        }
    }
}