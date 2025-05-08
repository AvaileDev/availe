package io.availe.mappers

import io.availe.jooq.enums.AllowedFormat as JooqAllowedFormat
import io.availe.jooq.enums.MessageType as JooqMessageType
import io.availe.models.createNLIPContent
import io.availe.openapi.model.AllowedFormat
import io.availe.openapi.model.NLIPRequest
import io.availe.openapi.model.NLIPSubMessage
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NLIPRequestMapperTest {

    @Test
    fun `test toRecords and fromRecords with simple request`() {
        // Create a simple NLIPRequest
        val uuid = UUID.randomUUID()
        val content = createNLIPContent(JsonPrimitive("Hello, world!"))
        val request = NLIPRequest(
            uuid = uuid,
            messagetype = NLIPRequest.Messagetype.control,
            format = AllowedFormat.text,
            subformat = "plain",
            content = content,
            submessages = null
        )

        // Convert to records
        val (parent, children) = NLIPRequestMapper.toRecords(request)

        // Verify parent record
        assertEquals(uuid, parent.uuid)
        assertEquals(JooqMessageType.control, parent.messagetype)
        assertEquals(JooqAllowedFormat.text, parent.format)
        assertEquals("plain", parent.subformat)
        assertNotNull(parent.content)

        // Verify children records
        assertEquals(0, children.size)

        // Convert back to NLIPRequest
        val reconstructed = NLIPRequestMapper.fromRecords(parent, children)

        // Verify reconstructed request
        assertEquals(uuid, reconstructed.uuid)
        assertEquals(NLIPRequest.Messagetype.control, reconstructed.messagetype)
        assertEquals(AllowedFormat.text, reconstructed.format)
        assertEquals("plain", reconstructed.subformat)
        assertNotNull(reconstructed.content)
        assertEquals(0, reconstructed.submessages?.size ?: 0)
    }

    @Test
    fun `test toRecords and fromRecords with submessages`() {
        // Create a NLIPRequest with submessages
        val uuid = UUID.randomUUID()
        val content = createNLIPContent(JsonPrimitive("Main content"))
        val submessage1 = NLIPSubMessage(
            format = AllowedFormat.text,
            subformat = "plain",
            content = createNLIPContent(JsonPrimitive("Submessage 1")),
            label = "label1"
        )
        val submessage2 = NLIPSubMessage(
            format = AllowedFormat.token,
            subformat = "embedding",
            content = createNLIPContent(JsonPrimitive("Submessage 2")),
            label = null
        )
        val request = NLIPRequest(
            uuid = uuid,
            messagetype = NLIPRequest.Messagetype.control,
            format = AllowedFormat.structured,
            subformat = "json",
            content = content,
            submessages = listOf(submessage1, submessage2)
        )

        // Convert to records
        val (parent, children) = NLIPRequestMapper.toRecords(request)

        // Verify parent record
        assertEquals(uuid, parent.uuid)
        assertEquals(JooqMessageType.control, parent.messagetype)
        assertEquals(JooqAllowedFormat.structured, parent.format)
        assertEquals("json", parent.subformat)
        assertNotNull(parent.content)

        // Verify children records
        assertEquals(2, children.size)
        assertEquals(JooqAllowedFormat.text, children[0].format)
        assertEquals("plain", children[0].subformat)
        assertNotNull(children[0].content)
        assertEquals("label1", children[0].label)
        assertEquals(JooqAllowedFormat.token, children[1].format)
        assertEquals("embedding", children[1].subformat)
        assertNotNull(children[1].content)
        assertEquals(null, children[1].label)

        // Convert back to NLIPRequest
        val reconstructed = NLIPRequestMapper.fromRecords(parent, children)

        // Verify reconstructed request
        assertEquals(uuid, reconstructed.uuid)
        assertEquals(NLIPRequest.Messagetype.control, reconstructed.messagetype)
        assertEquals(AllowedFormat.structured, reconstructed.format)
        assertEquals("json", reconstructed.subformat)
        assertNotNull(reconstructed.content)
        assertEquals(2, reconstructed.submessages?.size)
        assertEquals(AllowedFormat.text, reconstructed.submessages?.get(0)?.format)
        assertEquals("plain", reconstructed.submessages?.get(0)?.subformat)
        assertNotNull(reconstructed.submessages?.get(0)?.content)
        assertEquals("label1", reconstructed.submessages?.get(0)?.label)
        assertEquals(AllowedFormat.token, reconstructed.submessages?.get(1)?.format)
        assertEquals("embedding", reconstructed.submessages?.get(1)?.subformat)
        assertNotNull(reconstructed.submessages?.get(1)?.content)
        assertEquals(null, reconstructed.submessages?.get(1)?.label)
    }
}