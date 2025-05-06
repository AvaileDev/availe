package io.availe.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The formats defined by the NLIP spec.
 */
@Serializable
enum class AllowedFormat {
    text,
    token,
    structured,
    binary,
    location,
    generic
}

/**
 * One sub-message in a NLIP exchange.
 */
@Serializable
data class NLIPSubMessage(
    val format: AllowedFormat,
    val subformat: String,
    val content: JsonElement
)

/**
 * A top-level NLIP request/message.
 *
 * If you have no sub-messages, just leave `submessages` as an empty list,
 * and it behaves exactly like a “basic” message.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class NLIPRequest(
    /** true if this is a control/command message */
    val control: Boolean,

    /** one of text|token|… */
    val format: AllowedFormat,

    /** free-form “subformat” string, e.g. language name or token subtype */
    val subformat: String,

    /** the actual payload; can be JSON object or string */
    val content: JsonElement,

    /** any number of nested sub-messages; omit or leave empty if none */
    val submessages: List<NLIPSubMessage> = emptyList(),

    /** unique identifier for this request */
    val uuid: Uuid = Uuid.random()
)