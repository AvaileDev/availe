package io.availe.repositories

import io.availe.SELF_PORT
import io.availe.models.InternalMessage
import io.availe.models.OutboundMessage
import io.availe.models.Session
import io.availe.openapi.model.NLIPRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class KtorChatRepository(private val client: HttpClient) {
    private val sessionsUrl = "http://localhost:$SELF_PORT/api/chat/sessions"
    private val sessionId = "default"
    private val sessionUrl = "$sessionsUrl/$sessionId"

    @Serializable
    private data class CreateSessionRequest(val session: Session)

    @OptIn(ExperimentalTime::class)
    suspend fun createSession() {
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
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    suspend fun sendMessage(text: String, targetUrl: Url) {
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
        client.post("$sessionUrl/messages") {
            contentType(ContentType.Application.Json)
            setBody(OutboundMessage(targetUrl.toString(), userMsg))
        }
    }

    suspend fun getHistory(): List<InternalMessage> {
        // ensure session exists
        // (optional: skip this if you know it's already created)
        val resp: HttpResponse = client.get(sessionUrl)
        if (!resp.status.isSuccess()) {
            createSession()
        }
        // now load the messages
        return client.get("$sessionUrl/branches/root/messages").body()
    }
}
