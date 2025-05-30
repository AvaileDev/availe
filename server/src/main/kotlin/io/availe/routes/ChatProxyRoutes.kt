package io.availe.routes

import io.availe.SELF_HOSTS
import io.availe.SELF_PORT
import io.availe.client.NLIPClient
import io.availe.client.OllamaClient
import io.availe.models.BranchId
import io.availe.models.InternalMessage
import io.availe.models.OutboundMessage
import io.availe.services.ChatService
import io.availe.services.toApiError
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Route.chatProxyRoutes(internalChat: OllamaClient, httpClient: HttpClient) =
    route("/api/chat/sessions/{sessionId}/messages") {
        post {
            val sessionId = call.parameters["sessionId"]!!
            val outbound = call.receive<OutboundMessage>()
            ChatService.sendMessage(sessionId, BranchId.root, outbound.internalMessage).fold(
                { err -> return@post call.respond(HttpStatusCode.BadRequest, err.toApiError()) },
                {}
            )
            val text = outbound.internalMessage.nlipMessage.content
            val target = Url(outbound.targetUri)
            val replyText = if (target.host in SELF_HOSTS && target.port == SELF_PORT) {
                internalChat.generate(text)
            } else {
                NLIPClient(httpClient, target).ask(text).content
            }
            val generatedRequest = outbound.internalMessage.nlipMessage.copy(content = replyText)
            val reply = InternalMessage(
                id = Uuid.random().toString(),
                sessionId = sessionId,
                senderId = "agent-${Uuid.random()}",
                senderRole = InternalMessage.Role.AGENT,
                nlipMessage = generatedRequest,
                timeStamp = System.currentTimeMillis(),
                status = InternalMessage.Status.SENT
            )
            ChatService.sendMessage(sessionId, BranchId.root, reply).fold(
                { err -> return@post call.respond(HttpStatusCode.BadRequest, err.toApiError()) },
                {}
            )
            call.respondText(replyText, ContentType.Text.Plain)
        }
    }
