package io.availe.network

import io.ktor.http.*

interface ChatRepository {
    suspend fun sendMessage(
        text: String,
        conversationId: String? = null,
        targetUrl: Url
    ): String
}
