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
import io.availe.openapi.model.AllowedFormat

fun main() {
    embeddedServer(CIO, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    /* ---------- shared HTTP client ---------- */
    val http = HttpClient(io.ktor.client.engine.cio.CIO) {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json()
        }
    }

    val nlip = NLIPClient(http)
    val ollama = OllamaClient(http)

    /* ---------- routes ---------- */
    routing {
        get("/") {
            val html = this::class.java
                .classLoader
                .getResource("index.html")
                ?.readText()
                ?: error("index.html not found on classpath")
            call.respondText(html, ContentType.Text.Html)
        }

        get("/nlip/ping") {
            val reply = nlip.ask("Ping NLIP server!")
            call.respondText("NLIP replied: ${reply.content}")
        }

        get("/chat") {
            val user = call.request.queryParameters["q"] ?: ""
            val firstTurn = nlip.ask(user)
            val corr = firstTurn.submessages
                ?.firstOrNull { it.format == AllowedFormat.token && it.subformat == "correlator" }
                ?.content
//            call.respondText("NLIP said: ${firstTurn.content} (corr=$corr)")
            call.respondText(firstTurn.content)
        }
    }
}