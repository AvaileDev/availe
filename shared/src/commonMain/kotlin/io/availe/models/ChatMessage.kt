package io.availe.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val senderId: String,
    val textContent: String,
    val timeStamp: Long,
    val conversationId: String? = null
)
