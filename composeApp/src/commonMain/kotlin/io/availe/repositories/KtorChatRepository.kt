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
        _currentSessionId.value = "default"
    }

    /**
     * Fetches all available session identifiers
     */
    suspend fun getAllSessions(): Either<Throwable, List<String>> =
        Either.catch {
            val sessions = client.get(sessionsUrl).body<List<String>>()
            _availableSessions.update { sessions }
            sessions
        }

    /**
     * Sets the current session ID
     */
    fun setCurrentSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    /**
     * Creates a new session with a generated ID
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
            client.post(sessionsUrl) {
                contentType(ContentType.Application.Json)
                setBody(CreateSessionRequest(session))
            }
            
            // Update available sessions and set as current
            getAllSessions()
            _currentSessionId.value = sessionId
            
            sessionId
        }

    /**
     * Creates a session with the specified ID if it doesn't exist
     */
    @OptIn(ExperimentalTime::class)
    suspend fun createSession(sessionId: String = "default"): Either<Throwable, Unit> =
        Either.catch {
            val now = Clock.System.now().toEpochMilliseconds()
            val session = Session(
                id = sessionId,
                title = null,
                createdAt = now,
                lastActivityAt = now,
                participantIds = emptySet(),
                status = Session.Status.ACTIVE
            )
            client.post(sessionsUrl) {
                contentType(ContentType.Application.Json)
                setBody(CreateSessionRequest(session))
            }
            
            // Update available sessions
            getAllSessions()
            
            // Set as current if no current session
            if (_currentSessionId.value == null) {
                _currentSessionId.value = sessionId
            }
        }.map { }

    /**
     * Deletes a session by ID
     */
    suspend fun deleteSession(sessionId: String): Either<Throwable, Unit> =
        Either.catch {
            client.delete("$sessionsUrl/$sessionId")
            
            // Update available sessions
            getAllSessions()
            
            // If we deleted the current session, set to null
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = _availableSessions.value.firstOrNull()
            }
        }.map { }

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
     */
    suspend fun getHistory(): Either<Throwable, List<InternalMessage>> {
        val sessionId = _currentSessionId.value ?: return Either.catch { emptyList() }
        
        return Either.catch { client.get("$sessionsUrl/$sessionId") }
            .flatMap { resp ->
                if (!resp.status.isSuccess()) createSession(sessionId) else Unit.right()
            }
            .flatMap {
                Either.catch {
                    client.get("$sessionsUrl/$sessionId/branches/root/messages")
                        .body<List<InternalMessage>>()
                }
            }
    }
}