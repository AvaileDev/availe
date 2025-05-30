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
    suspend fun createSession(): Either<Throwable, Unit> =
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
        }.map { }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    suspend fun sendMessage(text: String, targetUrl: Url): Either<Throwable, Unit> =
        Either.catch {
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
        }.map { }

    suspend fun getHistory(): Either<Throwable, List<InternalMessage>> =
        Either.catch { client.get(sessionUrl) }
            .flatMap { resp ->
                if (!resp.status.isSuccess()) createSession() else Unit.right()
            }
            .flatMap {
                Either.catch {
                    client.get("$sessionUrl/branches/root/messages")
                        .body<List<InternalMessage>>()
                }
            }
}
