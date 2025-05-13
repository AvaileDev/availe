package io.availe.client

import io.availe.models.fromJson
import io.availe.models.toJson
import io.availe.openapi.model.AllowedFormat
import io.availe.openapi.model.NLIPRequest
import io.availe.openapi.model.NLIPSubMessage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*

class NLIPClient(private val http: HttpClient, private val baseUrl: Url) {

    private val endpoint: Url = URLBuilder(baseUrl).apply {
        encodedPath = encodedPath.removeSuffix("/") + "/nlip/"
    }.build()

    suspend fun ask(prompt: String, conversationId: String? = null): NLIPRequest {

        val conversationIdSubmessage = conversationId?.let {
            NLIPSubMessage(
                format = AllowedFormat.token,
                subformat = "conversation-id",
                content = it,
                label = null
            )
        }

        val req = NLIPRequest(
            messagetype = null,
            format      = AllowedFormat.text,
            subformat   = "English",
            content     = prompt,
            label = null,
            submessages = conversationIdSubmessage?.let { listOf(it) }
        )

        return http.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()
    }
}
