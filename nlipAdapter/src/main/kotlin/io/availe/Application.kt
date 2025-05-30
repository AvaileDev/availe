package io.availe

import io.availe.config.HttpClientProvider
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun main() {
    embeddedServer(CIO, port = SELF_PORT + 1, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val httpClient = HttpClientProvider.client

    routing {
        post("/compileSpec") {
            val body = call.receive<Map<String, String>>()
            val specUrl = body["specUrl"] ?: return@post call.respondText(
                "specUrl missing", status = io.ktor.http.HttpStatusCode.BadRequest
            )

            val raw = httpClient.get(specUrl).bodyAsText()

            if (!raw.contains("\"openapi\"")) {
                return@post call.respondText("Not an OpenAPI file", status = io.ktor.http.HttpStatusCode.BadRequest)
            }

            call.respondText("spec downloaded, size=${raw.length} bytes")
        }

        get("/health") {
            call.respondText("adapter up")
        }
    }
}