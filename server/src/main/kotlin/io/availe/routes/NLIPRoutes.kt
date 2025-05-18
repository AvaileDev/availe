package io.availe.routes

import io.availe.client.OllamaClient
import io.availe.models.ChatProxyRequest
import io.availe.openapi.model.NLIPRequest
import io.availe.services.ChatService
import io.availe.utils.normaliseUrl
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.NLIPRoutes(
    internalChat: OllamaClient,
    http: HttpClient
) = route("/nlip") {
    post {
        val proxyReq = call.receive<ChatProxyRequest>()
        val target = try {
            normaliseUrl(proxyReq.targetUrl)
        } catch (_: Exception) {
            return@post call.respond(HttpStatusCode.BadRequest, "Invalid targetUrl")
        }
        val incoming: NLIPRequest = proxyReq.message
        val reply = ChatService.forward(
            request = incoming,
            target = target,
            http = http,
            internalChat = internalChat
        )
        call.respondText(reply, ContentType.Text.Plain)
    }
}
