package io.availe.services

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val type: ApiErrorType,
    val sessionId: String? = null,
    val messageId: String? = null,
    val branchId: String? = null,
    val message: String? = null
)

@Serializable
enum class ApiErrorType {
    SESSION_NOT_FOUND,
    MESSAGE_NOT_FOUND,
    MESSAGE_ALREADY_EXISTS,
    SESSION_ALREADY_EXISTS,
    BRANCH_NOT_FOUND,
    SESSION_LIMIT_EXCEEDED,
    BRANCH_LIMIT_EXCEEDED,
    BRANCH_MESSAGE_LIMIT_EXCEEDED
}
