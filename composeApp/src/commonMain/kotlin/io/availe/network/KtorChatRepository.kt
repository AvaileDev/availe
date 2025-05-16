package io.availe.network

import io.availe.SERVER_PORT
import io.availe.models.ChatMessage
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class KtorChatRepository(
    private val httpClient: HttpClient,
) : ChatRepository {
    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun sendMessage(
        text: String,
        conversationId: String?,
        targetPort: Int
    ): String {
        val path = "/chat"

        val message = ChatMessage(
            id = Uuid.random().toString(),
            senderId = "mobile-client",
            textContent = text,
            timeStamp = Clock.System.now().toEpochMilliseconds(),
            conversationId = null
        )


        val url = "http://localhost:$SERVER_PORT/$targetPort/chat"
        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(message)
        }.bodyAsText()
    }
}
