package io.availe.models

import io.availe.openapi.model.NLIPRequest
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ChatProxyRequest @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid = Uuid.random(),
    val targetUrl: String,
    val message: NLIPRequest
)
