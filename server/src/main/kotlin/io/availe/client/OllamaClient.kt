package io.nvelo.client

import io.nvelo.models.OllamaModelQuery
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

class OllamaClient(private val httpClient: HttpClient) {
    companion object {
        private const val URL_PATH: String = "http://localhost:11434/api/generate"
    }

    /** ------------- Models ------------- */

    @Serializable
    private data class StreamChunk(
        val chunk: String = "",
        val done: Boolean = false
    )

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable
    @JsonIgnoreUnknownKeys
    private data class MinimalOllamaResponse(val response: String)

    /** ------------- Single-respond endpoint ------------- */

    suspend fun generate(prompt: String): String {
        val response = httpClient.post(URL_PATH) {
            contentType(ContentType.Application.Json)
            setBody(
                OllamaModelQuery(
                    model = "granite3.3:8b",
                    prompt = prompt,
                    stream = false
                )
            )
        }

        val minimal = response.body<MinimalOllamaResponse>()
        return minimal.response
    }

    /** ------------- Streaming endpoint ------------- */

    suspend fun generateStream(prompt: String): SharedFlow<String> {
        TODO("Not yet implemented")
    }
}