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
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID

class NLIPClient(private val http: HttpClient) {

    companion object {
        private const val URL = "http://localhost:8004/nlip/"
    }

    /**
     * Send one NLIP text request and return the server’s NLIP reply object.
     * Pass `correlator = null` for the very first turn.
     */
    suspend fun ask(prompt: String, correlator: String? = null): NLIPRequest {

        // ----- build request -----
        val subMsgs = correlator?.let {
            listOf(
                NLIPSubMessage(
                    format = AllowedFormat.token,
                    subformat = "correlator",
                    content = it
                )
            )
        }

        val req = NLIPRequest(
            uuid = UUID.randomUUID(),
            messagetype = null,
            format = AllowedFormat.text,
            subformat = "English",
            content = prompt,
            submessages = subMsgs
        )

        // ----- POST to /nlip/ -----
        val resp = http.post(URL) {
            contentType(ContentType.Application.Json)
            setBody(req.toJson())                        // String produced by NLIP_JSON
        }

        // Status‑code error?  throw first
        resp.body<Unit>()   // ensures raiseForStatus

        // Inject fallback UUID if missing
        val rawJson = resp.bodyAsText()
        val correctedJson = if (!rawJson.contains("\"uuid\"")) {
            rawJson.replaceFirst("{", "{\"uuid\":\"${UUID.randomUUID()}\",")
        } else rawJson

        // Decode back to Kotlin object
//        return NLIPRequest.fromJson(resp.bodyAsText())
        return NLIPRequest.fromJson(correctedJson)
    }
}
