package io.availe

import io.availe.client.NLIPClient
import io.availe.client.OllamaClient
import io.availe.config.HttpClientProvider
import io.availe.routes.chatProxyRoutes
import io.availe.routes.chatServiceRoutes
import io.availe.routes.healthRoutes
import io.availe.routes.staticRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(CIO, port = SELF_PORT, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true })
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                io.ktor.http.HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error", "message" to (cause.message ?: "Unknown error"))
            )
        }
    }
    val httpClient = HttpClientProvider.client
    val internalChat = OllamaClient(httpClient)
    val externalChat = NLIPClient(httpClient, Url("http://localhost:8004"))
    routing {
        staticRoutes()
        healthRoutes(externalChat)
        chatServiceRoutes()
        chatProxyRoutes(internalChat, httpClient)
    }
}
