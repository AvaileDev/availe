package io.availe.models

import io.availe.openapi.model.NLIPRequest
import kotlinx.serialization.Serializable

@Serializable
data class InternalMessage(
    val id: String,
    val sessionId: String,
    val senderId: String,
    val senderRole: Role,
    val nlipMessage: NLIPRequest,
    val timeStamp: Long,
    val status: Status
) {
    @Serializable
    enum class Role { USER, AGENT, SYSTEM, AUDITOR, OTHER }

    @Serializable
    enum class Status { PENDING, SENT, RECEIVED, ERROR }
}
