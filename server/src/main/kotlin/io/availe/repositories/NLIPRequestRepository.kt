package io.nvelo.repositories

import io.nvelo.jooq.tables.NlipRequest.NLIP_REQUEST
import io.nvelo.jooq.tables.NlipSubmessage.NLIP_SUBMESSAGE
import io.nvelo.mappers.NLIPRequestMapper
import io.nvelo.models.NLIPRequest
import org.jooq.DSLContext
import java.util.UUID

class NLIPRequestRepository(private val dsl: DSLContext) {

    /**
     * Persist a complete NLIPRequest (with sub‑messages) in one transaction.
     */
    fun save(request: NLIPRequest) {
        val (parent, children) = NLIPRequestMapper.toRecords(request)
        dsl.transaction { cfg ->
            cfg.dsl().insertInto(NLIP_REQUEST).set(parent).execute()
            if (children.isNotEmpty()) {
                // Set the request_uuid for each submessage
                val childrenWithRequestUuid = children.map { child ->
                    child.apply {
                        requestUuid = parent.uuid
                    }
                }
                cfg.dsl().batchInsert(childrenWithRequestUuid).execute()
            }
        }
    }

    /**
     * Fetch a full NLIPRequest by UUID. Returns null if not found.
     */
    fun find(uuid: UUID): NLIPRequest? {
        val parent = dsl.selectFrom(NLIP_REQUEST)
            .where(NLIP_REQUEST.UUID.eq(uuid))
            .fetchOne()
            ?: return null

        val children = dsl.selectFrom(NLIP_SUBMESSAGE)
            .where(NLIP_SUBMESSAGE.REQUEST_UUID.eq(uuid))
            .orderBy(NLIP_SUBMESSAGE.ID.asc()) // preserves insertion order
            .fetch()

        return NLIPRequestMapper.fromRecords(parent, children)
    }
}
