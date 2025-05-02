package io.availe.models

import kotlinx.serialization.Serializable

@Serializable
data class OllamaModelQuery(
    val model: String,
    val prompt: String,
    val stream: Boolean = false
)