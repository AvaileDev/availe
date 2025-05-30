package io.availe.models

import kotlinx.serialization.Serializable

@Serializable
data class OutboundMessage(
    val targetUri: String,
    val internalMessage: InternalMessage,
)
