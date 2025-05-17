package io.availe.routes

import io.availe.SERVER_PORT
import io.availe.client.NLIPClient
import io.availe.client.OllamaClient
import io.availe.models.ChatMessage
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.chatRoute(internalChat: OllamaClient, httpClient: HttpClient) {
    post("/{targetPort}/chat") {
        val targetPort = call.parameters["targetPort"]?.toIntOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid port")

        val incoming = call.receive<ChatMessage>()

        if (targetPort == SERVER_PORT) {
            val responseText = internalChat.generate(incoming.textContent)
            call.respondText(responseText, ContentType.Text.Plain)
        } else {
            call.respondTextWriter(contentType = ContentType.Text.Plain) {
                handleNLIPChatResponse(httpClient, targetPort, incoming) { text ->
                    write(text)
                    flush()
                }
            }
        }
    }
}

suspend fun handleNLIPChatResponse(
    httpClient: HttpClient,
    targetPort: Int,
    incoming: ChatMessage,
    writer: (String) -> Unit
) {
    try {
        val dynamicExternalChat = NLIPClient(httpClient, Url("http://localhost:$targetPort"))
        val response = dynamicExternalChat.ask(incoming.textContent)
        response.content.lineSequence().forEach {
            writer(it)
            writer("\n")
        }
    } catch (e: Exception) {
        writer("Error: ${e.message ?: "Unknown"}\n")
    }
}
