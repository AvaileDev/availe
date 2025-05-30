package io.availe.services

sealed interface ChatError {
    data class SessionNotFound(val sessionId: String) : ChatError
    data class MessageNotFound(val sessionId: String, val messageId: String) : ChatError
    data class MessageAlreadyExists(val sessionId: String, val messageId: String) : ChatError
    data class SessionAlreadyExists(val sessionId: String) : ChatError
    data class BranchNotFound(val sessionId: String, val branchId: String) : ChatError
    data class SessionLimitExceeded(val sessionId: String) : ChatError
    data class BranchLimitExceeded(val sessionId: String) : ChatError
    data class BranchMessageLimitExceeded(val sessionId: String, val branchId: String) : ChatError
}
