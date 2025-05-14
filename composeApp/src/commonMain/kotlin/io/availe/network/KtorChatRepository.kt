package io.availe.network

import io.availe.SERVER_PORT
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

class KtorChatRepository(
    private val httpClient: HttpClient,
    private val port: Int = SERVER_PORT
) : ChatRepository {
    override suspend fun sendMessage(
        text: String,
        target: ChatTarget,
        conversationId: String?
    ): String {
        val path = when (target) {
            ChatTarget.Internal -> "/chat"
            ChatTarget.External -> "/chat/external"
        }
        return httpClient.get("http://localhost:$port$path") {
            parameter("q", text)
            conversationId?.let { parameter("conversationId", it) }
        }.bodyAsText()
    }
}
