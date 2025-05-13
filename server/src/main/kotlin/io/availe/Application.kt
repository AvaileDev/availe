package io.availe

import io.availe.client.NLIPClient
import io.availe.client.OllamaClient
import io.ktor.client.*
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.Url
import io.ktor.server.http.content.staticResources

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

    val internalChat = OllamaClient(httpClient)
    val externalChat = NLIPClient(httpClient, Url("http://localhost:8004"))

    routing {
        staticResources("/static", basePackage = "static")

        get("/") {
            val html = this::class.java
                .classLoader
                .getResource("index.html")
                ?.readText()
                ?: error("index.html not found on classpath")
            call.respondText(html, ContentType.Text.Html)
        }

        get("/nlip/ping") {
            val reply = externalChat.ask("Ping NLIP server!")
            call.respondText("NLIP replied: ${reply.content}")
        }

        route("/chat") {
            get {
                val userQ = call.request.queryParameters["q"] ?: ""
                val reply = internalChat.generate(userQ)
                call.respondText(reply)
            }

            get("/external") {
                val userQ = call.request.queryParameters["q"] ?: ""
                val reply = externalChat.ask(userQ)
                call.respondText(reply.content)
            }
        }
    }
}
