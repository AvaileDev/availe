package io.availe.models

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val title: String?,
    val createdAt: Long,
    val lastActivityAt: Long,
    val participantIds: Set<String>,
    val status: Status
) {
    @Serializable
    enum class Status { ACTIVE, ARCHIVED, LOCAL, TEMPORARY }
}
