package io.availe.services

import io.ktor.http.*

fun ChatError.toApiError(): ApiError = when (this) {
    is ChatError.SessionNotFound -> ApiError(ApiErrorType.SESSION_NOT_FOUND, sessionId = sessionId)
    is ChatError.MessageNotFound -> ApiError(
        ApiErrorType.MESSAGE_NOT_FOUND,
        sessionId = sessionId,
        messageId = messageId
    )

    is ChatError.MessageAlreadyExists -> ApiError(
        ApiErrorType.MESSAGE_ALREADY_EXISTS,
        sessionId = sessionId,
        messageId = messageId
    )

    is ChatError.SessionAlreadyExists -> ApiError(ApiErrorType.SESSION_ALREADY_EXISTS, sessionId = sessionId)
    is ChatError.BranchNotFound -> ApiError(ApiErrorType.BRANCH_NOT_FOUND, sessionId = sessionId, branchId = branchId)
    is ChatError.SessionLimitExceeded -> ApiError(ApiErrorType.SESSION_LIMIT_EXCEEDED, sessionId = sessionId)
    is ChatError.BranchLimitExceeded -> ApiError(ApiErrorType.BRANCH_LIMIT_EXCEEDED, sessionId = sessionId)
    is ChatError.BranchMessageLimitExceeded -> ApiError(
        ApiErrorType.BRANCH_MESSAGE_LIMIT_EXCEEDED,
        sessionId = sessionId,
        branchId = branchId
    )
}

fun ChatError.toStatusCode(): HttpStatusCode = when (this) {
    is ChatError.SessionNotFound,
    is ChatError.MessageNotFound,
    is ChatError.BranchNotFound -> HttpStatusCode.NotFound

    is ChatError.MessageAlreadyExists,
    is ChatError.SessionAlreadyExists -> HttpStatusCode.Conflict

    is ChatError.SessionLimitExceeded,
    is ChatError.BranchLimitExceeded,
    is ChatError.BranchMessageLimitExceeded -> HttpStatusCode.TooManyRequests
}