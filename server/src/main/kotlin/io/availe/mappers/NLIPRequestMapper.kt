package io.availe.mappers

import io.availe.jooq.tables.records.NlipRequestRecord
import io.availe.jooq.tables.records.NlipSubmessageRecord
import io.availe.models.createNLIPContent
import io.availe.models.getJsonElement
import io.availe.models.toContentJsonb
import io.availe.models.toNLIPContent
import io.availe.openapi.model.NLIPRequest
import io.availe.openapi.model.NLIPSubMessage
import java.util.*
import io.availe.jooq.enums.AllowedFormat as JooqAllowedFormat
import io.availe.jooq.enums.MessageType as JooqMessageType
import io.availe.openapi.model.AllowedFormat as ModelAllowedFormat

object NLIPRequestMapper {
    /** ------------- Write to SQL DB ------------- */
    fun toRecords(dto: NLIPRequest): Pair<NlipRequestRecord, List<NlipSubmessageRecord>> {
        val parent = NlipRequestRecord().apply {
            // Convert messagetype enum to string if not null, otherwise use null
            messagetype = dto.messagetype?.let { JooqMessageType.valueOf(it.value) }
            format = JooqAllowedFormat.valueOf(dto.format.value)
            subformat = dto.subformat
            content = dto.content.toContentJsonb()
            uuid = dto.uuid
        }

        val children = dto.submessages?.map { submessage ->
            NlipSubmessageRecord().apply {
                requestUuid = dto.uuid
                format = JooqAllowedFormat.valueOf(submessage.format.value)
                subformat = submessage.subformat
                content = submessage.content.toContentJsonb()
                label = submessage.label
            }
        } ?: emptyList()

        return parent to children
    }

    /** ------------- Get record from SQL DB ------------- */
    fun fromRecords(
        parent: NlipRequestRecord,
        children: List<NlipSubmessageRecord>
    ): NLIPRequest = NLIPRequest(
        uuid = parent.uuid,
        messagetype = parent.messagetype?.let { NLIPRequest.Messagetype.valueOf(it.name) },
        format = ModelAllowedFormat.valueOf(parent.format.name),
        subformat = parent.subformat,
        content = parent.content.toNLIPContent(),
        submessages = children.map {
            NLIPSubMessage(
                format = ModelAllowedFormat.valueOf(it.format.name),
                subformat = it.subformat,
                content = it.content.toNLIPContent(),
                label = it.label
            )
        }
    )
}
