package io.availe.network

interface ChatRepository {
    suspend fun sendMessage(
        text: String,
        conversationId: String? = null,
        targetPort: Int
    ): String
}
