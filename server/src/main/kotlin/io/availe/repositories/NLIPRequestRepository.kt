package io.availe.repositories

import io.availe.jooq.tables.NlipRequest.NLIP_REQUEST
import io.availe.jooq.tables.NlipSubmessage.NLIP_SUBMESSAGE
import io.availe.mappers.NlipRequestMapper
import io.availe.openapi.model.NLIPRequest
import org.jooq.DSLContext
import org.mapstruct.factory.Mappers
import java.util.UUID

class NLIPRequestRepository(private val dsl: DSLContext) {

    private val mapper: NlipRequestMapper =
        Mappers.getMapper(NlipRequestMapper::class.java)

    /* ---------- save ---------------------------------------------------- */
    fun save(request: NLIPRequest) {
        val parent = mapper.toEntity(request)
        val children = mapper
            .toSubEntities(request.submessages ?: emptyList())

        dsl.transaction { cfg ->
            cfg.dsl().insertInto(NLIP_REQUEST).set(parent).execute()

            if (children.isNotEmpty()) {
                children.forEach { it.requestId = parent.id }
                cfg.dsl().batchInsert(children).execute()
            }
        }
    }

    /* ---------- find ---------------------------------------------------- */
    fun find(id: UUID): NLIPRequest? {
        val parent = dsl.selectFrom(NLIP_REQUEST)
            .where(NLIP_REQUEST.ID.eq(id))
            .fetchOne()
            ?: return null

        val children = dsl.selectFrom(NLIP_SUBMESSAGE)
            .where(NLIP_SUBMESSAGE.REQUEST_ID.eq(id))
            .orderBy(NLIP_SUBMESSAGE.ID.asc())
            .fetch()

        return mapper.fromEntity(parent, children)
    }
}
