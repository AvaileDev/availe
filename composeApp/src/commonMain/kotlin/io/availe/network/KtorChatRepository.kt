package io.availe.network

import io.availe.SERVER_PORT
import io.availe.models.ChatProxyRequest
import io.availe.openapi.model.AllowedFormat
import io.availe.openapi.model.NLIPRequest
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

class KtorChatRepository(
    private val httpClient: HttpClient,
) : ChatRepository {
    private val proxyUrl = Url("http://localhost:$SERVER_PORT/nlip")

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun sendMessage(
        text: String,
        conversationId: String?,
        targetUrl: Url
    ): String {
        val nlipRequest = NLIPRequest(
            messagetype = null,
            format = AllowedFormat.text,
            subformat = "English",
            content = text,
            label = null,
            submessages = null
        )

        val proxyReq = ChatProxyRequest(
            targetUrl = targetUrl.toString(),
            message = nlipRequest
        )

        return httpClient.post(proxyUrl) {
            contentType(ContentType.Application.Json)
            setBody(proxyReq)
        }.bodyAsText()
    }
}
