package io.nvelo.mappers

import io.nvelo.jooq.tables.records.NlipRequestRecord
import io.nvelo.jooq.tables.records.NlipSubmessageRecord
import io.nvelo.models.NLIPRequest
import io.nvelo.models.NLIPSubMessage
import io.nvelo.models.toJsonElement
import io.nvelo.models.toJsonb
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid
import io.nvelo.jooq.enums.AllowedFormat as JooqAllowedFormat
import io.nvelo.models.AllowedFormat as ModelAllowedFormat


@OptIn(ExperimentalUuidApi::class)
fun Uuid.toJvmUUID(): UUID =
    UUID.fromString(this.toString())

object NLIPRequestMapper {
    /** ------------- Write to SQL DB ------------- */
    @OptIn(ExperimentalUuidApi::class)
    fun toRecords(dto: NLIPRequest): Pair<NlipRequestRecord, List<NlipSubmessageRecord>> {
        val parent = NlipRequestRecord().apply {
            control = dto.control
            format = JooqAllowedFormat.valueOf(dto.format.name)
            subformat = dto.subformat
            content = dto.content.toJsonb()
            uuid = dto.uuid.toJvmUUID()
        }

        val children = dto.submessages.map { submessage ->
            NlipSubmessageRecord().apply {
                format = JooqAllowedFormat.valueOf(submessage.format.name)
                subformat = submessage.subformat
                content = submessage.content.toJsonb()
            }
        }

        return parent to children
    }

    /** ------------- Get record from SQL DB ------------- */
    @OptIn(ExperimentalUuidApi::class)
    fun fromRecords(
        parent: NlipRequestRecord,
        children: List<NlipSubmessageRecord>
    ): NLIPRequest = NLIPRequest(
        uuid = parent.uuid.toKotlinUuid(),
        control = parent.control,
        format = ModelAllowedFormat.valueOf(parent.format.name),
        subformat = parent.subformat,
        content = parent.content.toJsonElement(),
        submessages = children.map {
            NLIPSubMessage(
                format = ModelAllowedFormat.valueOf(it.format.name),
                subformat = it.subformat,
                content = it.content.toJsonElement()
            )
        }
    )
}