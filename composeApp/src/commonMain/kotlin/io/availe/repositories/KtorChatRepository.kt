package io.availe.repositories

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import io.availe.SELF_PORT
import io.availe.models.InternalMessage
import io.availe.models.OutboundMessage
import io.availe.models.Session
import io.availe.openapi.model.NLIPRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.delete
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class KtorChatRepository(private val client: HttpClient) {

    private val sessionsUrl = "http://localhost:$SELF_PORT/api/chat/sessions"
    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _availableSessions = MutableStateFlow<List<String>>(emptyList())
    val availableSessions: StateFlow<List<String>> = _availableSessions.asStateFlow()

    @Serializable
    private data class CreateSessionRequest(val session: Session)

    init {
        // Initialize with default session if available
        println("KtorChatRepository: Initializing with default session")
        _currentSessionId.value = "default"
        println("KtorChatRepository: Current session ID set to: ${_currentSessionId.value}")
    }

    /**
     * Fetches all available session identifiers from the server and updates the local state
     * @return Either with potential error or the list of session identifiers
     */
    suspend fun getAllSessions(): Either<Throwable, List<String>> =
        Either.catch {
            val response = client.get(sessionsUrl)
            if (!response.status.isSuccess()) {
                throw RuntimeException("Failed to fetch sessions: ${response.status}")
            }

            // The server returns a SessionListResponse with a sessionIds field
            @Serializable
            data class SessionListResponse(val sessionIds: List<String>)

            val sessionListResponse = response.body<SessionListResponse>()
            println("Received sessions from server: ${sessionListResponse.sessionIds}")
            sessionListResponse.sessionIds
        }.map { serverSessions ->
            // Update the local state with the server data
            _availableSessions.update { serverSessions }
            serverSessions
        }

    /**
     * Sets the current session ID
     */
    fun setCurrentSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    /**
     * Creates a new session with a generated ID
     * @param title Optional title for the session
     * @return Either with potential error or the created session ID
     */
    @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
    suspend fun createNewSession(title: String? = null): Either<Throwable, String> =
        Either.catch {
            val now = Clock.System.now().toEpochMilliseconds()
            val sessionId = Uuid.random().toString()
            val session = Session(
                id = sessionId,
                title = title,
                createdAt = now,
                lastActivityAt = now,
                participantIds = emptySet(),
                status = Session.Status.ACTIVE
            )
            val response = client.post(sessionsUrl) {
                contentType(ContentType.Application.Json)
                setBody(CreateSessionRequest(session))
            }

            if (!response.status.isSuccess()) {
                throw RuntimeException("Failed to create session: ${response.status}")
            }

            sessionId
        }.flatMap { newSessionId ->
            // Update available sessions
            getAllSessions().map { newSessionId }
        }.map { newSessionId ->
            // Set as current session
            _currentSessionId.value = newSessionId
            newSessionId
        }

    /**
     * Creates a session with the specified ID if it doesn't exist
     * @param sessionId ID for the session, defaults to "default"
     * @return Either with potential error or Unit on success
     */
    @OptIn(ExperimentalTime::class)
    suspend fun createSession(sessionId: String = "default"): Either<Throwable, Unit> =
        Either.catch {
            println("KtorChatRepository: Creating session with ID: $sessionId")
            val now = Clock.System.now().toEpochMilliseconds()
            val session = Session(
                id = sessionId,
                title = null,
                createdAt = now,
                lastActivityAt = now,
                participantIds = emptySet(),
                status = Session.Status.ACTIVE
            )
            val response = client.post(sessionsUrl) {
                contentType(ContentType.Application.Json)
                setBody(CreateSessionRequest(session))
            }

            if (!response.status.isSuccess()) {
                println("KtorChatRepository: Failed to create session: ${response.status}")
                throw RuntimeException("Failed to create session: ${response.status}")
            }
            println("KtorChatRepository: Session created successfully")
        }.flatMap {
            // Update available sessions
            println("KtorChatRepository: Updating available sessions after creating session")
            getAllSessions().map { }
        }.map {
            // Set as current if no current session
            if (_currentSessionId.value == null) {
                println("KtorChatRepository: Setting current session to: $sessionId")
                _currentSessionId.value = sessionId
            }
        }

    /**
     * Deletes a session by ID
     * @param sessionId ID of the session to delete
     * @return Either with potential error or Unit on success
     */
    suspend fun deleteSession(sessionId: String): Either<Throwable, Unit> =
        Either.catch {
            val response = client.delete("$sessionsUrl/$sessionId")

            if (!response.status.isSuccess()) {
                throw RuntimeException("Failed to delete session: ${response.status}")
            }
        }.flatMap {
            // Update available sessions
            getAllSessions().map { }
        }.map {
            // If we deleted the current session, set to the first available session or null
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = _availableSessions.value.firstOrNull()
            }
        }

    /**
     * Sends a message to the current session
     */
    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    suspend fun sendMessage(text: String, targetUrl: Url): Either<Throwable, Unit> =
        Either.catch {
            val sessionId = _currentSessionId.value ?: return@catch

            val request = NLIPRequest(
                format = io.availe.openapi.model.AllowedFormat.text,
                subformat = "English",
                content = text
            )
            val userMsg = InternalMessage(
                id = Uuid.random().toString(),
                sessionId = sessionId,
                senderId = "user-${Uuid.random()}",
                senderRole = InternalMessage.Role.USER,
                nlipMessage = request,
                timeStamp = Clock.System.now().toEpochMilliseconds(),
                status = InternalMessage.Status.PENDING
            )
            client.post("$sessionsUrl/$sessionId/messages") {
                contentType(ContentType.Application.Json)
                setBody(OutboundMessage(targetUrl.toString(), userMsg))
            }
        }.map { }

    /**
     * Gets message history for the current session
     * @return Either with potential error or the list of messages
     */
    suspend fun getHistory(): Either<Throwable, List<InternalMessage>> {
        val sessionId = _currentSessionId.value ?: return emptyList<InternalMessage>().right()

        return Either.catch { client.get("$sessionsUrl/$sessionId") }
            .flatMap { resp ->
                if (!resp.status.isSuccess()) {
                    // Session doesn't exist, create it
                    createSession(sessionId).map { emptyList<InternalMessage>() }
                } else {
                    // Session exists, get messages
                    Either.catch {
                        val messagesResponse = client.get("$sessionsUrl/$sessionId/branches/root/messages")
                        if (!messagesResponse.status.isSuccess()) {
                            throw RuntimeException("Failed to fetch messages: ${messagesResponse.status}")
                        }
                        messagesResponse.body<List<InternalMessage>>()
                    }
                }
            }
    }
}
