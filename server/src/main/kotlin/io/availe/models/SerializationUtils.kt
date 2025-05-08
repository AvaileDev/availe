package io.availe.models

import io.availe.openapi.model.NLIPRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jooq.JSONB

internal val NLIP_JSON = Json { encodeDefaults = true }

/**--- NLIPRequest ↔ JSON String ---*/

fun NLIPRequest.toJson(): String =
    NLIP_JSON.encodeToString(this)

fun NLIPRequest.Companion.fromJson(rawJson: String): NLIPRequest =
    NLIP_JSON.decodeFromString(rawJson)

/**--- JSONB ↔ JsonElement ---*/

internal fun JSONB.toJsonElement(): JsonElement =
    NLIP_JSON.decodeFromString(this.data())

/**--- Any @Serializable → JSONB ---*/

internal inline fun <reified T> T.toJsonb(): JSONB =
    JSONB.valueOf(NLIP_JSON.encodeToString(this))
