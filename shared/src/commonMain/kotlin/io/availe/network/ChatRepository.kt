package io.availe.network

enum class ChatTarget { Internal, External }

interface ChatRepository {
    suspend fun sendMessage(
        text: String,
        target: ChatTarget,
        conversationId: String? = null
    ): String
}
