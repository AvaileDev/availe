package io.availe.services

import io.availe.SERVER_PORT
import io.availe.client.NLIPClient
import io.availe.client.OllamaClient
import io.availe.openapi.model.NLIPRequest
import io.availe.utils.isNLIPEndpoint
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object ChatService {
    suspend fun forward(
        request: NLIPRequest,
        target: Url,
        http: HttpClient,
        internalChat: OllamaClient
    ): String = when {
        // use Ollama adapter as NLIP endpoint
        target.host == "localhost" &&
                target.port == SERVER_PORT &&
                isNLIPEndpoint(target) ->
            internalChat.generate(request.content)

        // use official NLIP endpoint
        isNLIPEndpoint(target) ->
            NLIPClient(http, target).ask(
                prompt = request.content,
                conversationId = request.submessages
                    ?.firstOrNull { it.subformat == "conversation-id" }
                    ?.content
            ).content

        // Fallback: send NLIP JSON to non-NLIP endpoint
        else ->
            http.post(target) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.bodyAsText()
    }
}