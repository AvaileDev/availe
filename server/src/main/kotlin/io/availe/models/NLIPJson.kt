package io.availe.models

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jooq.JSONB

internal val NLIP_JSON = Json { encodeDefaults = true }

/** ------------- JSON helpers ------------- */

fun NLIPRequest.toJson(): String {
    return NLIP_JSON.encodeToString(NLIPRequest.serializer(), this)
}

fun NLIPRequest.Companion.fromJson(rawJson: String): NLIPRequest {
    return NLIP_JSON.decodeFromString(NLIPRequest.serializer(), rawJson)
}

/** ------------- JSON to JOSNB helpers ------------- */

internal fun JsonElement.toJsonb(): JSONB {
    return JSONB.valueOf(
        NLIP_JSON.encodeToString(JsonElement.serializer(), this)
    )
}

internal fun JSONB.toJsonElement(): JsonElement {
    return NLIP_JSON.decodeFromString((JsonElement.serializer()), this.data())
}