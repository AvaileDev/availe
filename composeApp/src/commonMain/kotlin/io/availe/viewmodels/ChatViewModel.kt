package io.availe.viewmodels

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import io.availe.models.InternalMessage
import io.availe.repositories.KtorChatRepository
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: KtorChatRepository
) {
    private val _messages = MutableStateFlow<List<InternalMessage>>(emptyList())
    val messages: StateFlow<List<InternalMessage>> = _messages.asStateFlow()

    // Expose session-related state from the repository
    val availableSessions = repository.availableSessions
    val currentSessionId = repository.currentSessionId

    // UI state for session creation
    private val _isCreatingSession = MutableStateFlow(false)
    val isCreatingSession: StateFlow<Boolean> = _isCreatingSession.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    init {
        scope.launch {
            // Fetch available sessions
            repository.getAllSessions()
                .fold(
                    { /* handle error if needed */ },
                    { /* success */ }
                )

            // Create default session if needed
            repository.createSession()
                .fold(
                    { /* handle error if needed */ },
                    { /* success */ }
                )

            // Observe current session changes to update messages
            repository.currentSessionId.collectLatest { sessionId ->
                if (sessionId != null) {
                    refreshMessages()
                } else {
                    _messages.value = emptyList()
                }
            }
        }
    }

    /**
     * Refreshes the message list for the current session
     */
    private suspend fun refreshMessages() {
        repository.getHistory()
            .fold(
                { _messages.value = emptyList() },
                { history -> _messages.value = history }
            )
    }

    /**
     * Sends a message in the current session
     */
    fun send(text: String, targetUrl: Url) {
        scope.launch {
            repository.sendMessage(text, targetUrl)
                .flatMap { repository.getHistory() }
                .fold(
                    { _messages.value = emptyList() },
                    { history -> _messages.value = history }
                )
        }
    }

    /**
     * Creates a new chat session with optional title
     */
    fun createSession(title: String? = null) {
        scope.launch {
            _isCreatingSession.value = true
            repository.createNewSession(title)
                .fold(
                    { /* handle error */ },
                    { /* success */ }
                )
            _isCreatingSession.value = false
        }
    }

    /**
     * Selects a session by ID
     */
    fun selectSession(sessionId: String) {
        repository.setCurrentSession(sessionId)
    }

    /**
     * Deletes a session by ID
     * @return Either with potential error or Unit on success
     */
    fun deleteSession(sessionId: String): Either<Throwable, Unit> = either {
        scope.launch {
            repository.deleteSession(sessionId)
                .fold(
                    { error -> 
                        // Log error or handle it appropriately
                        println("Error deleting session: ${error.message}")
                    },
                    { /* Session deleted successfully */ }
                )
        }
        Unit
    }
}
