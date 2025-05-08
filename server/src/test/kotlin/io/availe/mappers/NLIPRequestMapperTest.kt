package io.availe.mappers

import io.availe.jooq.enums.AllowedFormat as JooqAllowedFormat
import io.availe.jooq.enums.MessageType as JooqMessageType
import io.availe.openapi.model.AllowedFormat as ModelAllowedFormat
import io.availe.openapi.model.NLIPRequest
import io.availe.openapi.model.NLIPSubMessage
import org.jooq.JSONB
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class NLIPRequestMapperTest {

    @Test
    fun `test toRecords with submessages`() {
        // Arrange
        val uuid = UUID.randomUUID()
        val request = NLIPRequest(
            uuid = uuid,
            messagetype = NLIPRequest.Messagetype.control,
            format = ModelAllowedFormat.text,
            subformat = "plain",
            content = "Hello, world!",
            submessages = listOf(
                NLIPSubMessage(
                    format = ModelAllowedFormat.structured,
                    subformat = "json",
                    content = """{"greeting": "Hello"}""",
                    label = "metadata"
                ),
                NLIPSubMessage(
                    format = ModelAllowedFormat.binary,
                    subformat = "image",
                    content = "base64data",
                    label = null
                )
            )
        )

        // Act
        val (parent, children) = NLIPRequestMapper.toRecords(request)

        // Assert
        assertEquals(uuid, parent.uuid)
        assertEquals(JooqMessageType.control, parent.messagetype)
        assertEquals(JooqAllowedFormat.text, parent.format)
        assertEquals("plain", parent.subformat)
        assertEquals("\"Hello, world!\"", parent.content.data())

        assertEquals(2, children.size)

        val firstChild = children[0]
        assertEquals(uuid, firstChild.requestUuid)
        assertEquals(JooqAllowedFormat.structured, firstChild.format)
        assertEquals("json", firstChild.subformat)
        assertEquals(""""{\"greeting\": \"Hello\"}"""", firstChild.content.data())
        assertEquals("metadata", firstChild.label)

        val secondChild = children[1]
        assertEquals(uuid, secondChild.requestUuid)
        assertEquals(JooqAllowedFormat.binary, secondChild.format)
        assertEquals("image", secondChild.subformat)
        assertEquals("\"base64data\"", secondChild.content.data())
        assertNull(secondChild.label)
    }

    @Test
    fun `test toRecords without submessages`() {
        // Arrange
        val uuid = UUID.randomUUID()
        val request = NLIPRequest(
            uuid = uuid,
            messagetype = null,
            format = ModelAllowedFormat.token,
            subformat = "embedding",
            content = "token data",
            submessages = null
        )

        // Act
        val (parent, children) = NLIPRequestMapper.toRecords(request)

        // Assert
        assertEquals(uuid, parent.uuid)
        assertNull(parent.messagetype)
        assertEquals(JooqAllowedFormat.token, parent.format)
        assertEquals("embedding", parent.subformat)
        assertEquals("\"token data\"", parent.content.data())
        assertEquals(0, children.size)
    }

    @Test
    fun `test fromRecords with submessages`() {
        // Arrange
        val uuid = UUID.randomUUID()
        val parent = createNlipRequestRecord(
            uuid = uuid,
            messagetype = JooqMessageType.control,
            format = JooqAllowedFormat.text,
            subformat = "plain",
            content = "Hello, world!"
        )

        val children = listOf(
            createNlipSubmessageRecord(
                requestUuid = uuid,
                format = JooqAllowedFormat.structured,
                subformat = "json",
                content = """{"greeting": "Hello"}""",
                label = "metadata"
            ),
            createNlipSubmessageRecord(
                requestUuid = uuid,
                format = JooqAllowedFormat.binary,
                subformat = "image",
                content = "base64data",
                label = null
            )
        )

        // Act
        val result = NLIPRequestMapper.fromRecords(parent, children)

        // Assert
        assertEquals(uuid, result.uuid)
        assertEquals(NLIPRequest.Messagetype.control, result.messagetype)
        assertEquals(ModelAllowedFormat.text, result.format)
        assertEquals("plain", result.subformat)
        assertEquals("Hello, world!", result.content)

        assertEquals(2, result.submessages?.size)

        val firstSubmessage = result.submessages?.get(0)
        assertEquals(ModelAllowedFormat.structured, firstSubmessage?.format)
        assertEquals("json", firstSubmessage?.subformat)
        assertEquals("""{"greeting": "Hello"}""", firstSubmessage?.content)
        assertEquals("metadata", firstSubmessage?.label)

        val secondSubmessage = result.submessages?.get(1)
        assertEquals(ModelAllowedFormat.binary, secondSubmessage?.format)
        assertEquals("image", secondSubmessage?.subformat)
        assertEquals("base64data", secondSubmessage?.content)
        assertNull(secondSubmessage?.label)
    }

    @Test
    fun `test fromRecords without submessages`() {
        // Arrange
        val uuid = UUID.randomUUID()
        val parent = createNlipRequestRecord(
            uuid = uuid,
            messagetype = null,
            format = JooqAllowedFormat.token,
            subformat = "embedding",
            content = "token data"
        )

        val children = emptyList<io.availe.jooq.tables.records.NlipSubmessageRecord>()

        // Act
        val result = NLIPRequestMapper.fromRecords(parent, children)

        // Assert
        assertEquals(uuid, result.uuid)
        assertNull(result.messagetype)
        assertEquals(ModelAllowedFormat.token, result.format)
        assertEquals("embedding", result.subformat)
        assertEquals("token data", result.content)
        assertEquals(0, result.submessages?.size)
    }

    // Helper methods to create test records
    private fun createNlipRequestRecord(
        uuid: UUID,
        messagetype: JooqMessageType?,
        format: JooqAllowedFormat,
        subformat: String,
        content: String
    ): io.availe.jooq.tables.records.NlipRequestRecord {
        val record = io.availe.jooq.tables.records.NlipRequestRecord()
        record.uuid = uuid
        record.messagetype = messagetype
        record.format = format
        record.subformat = subformat
        record.content = JSONB.valueOf("\"" + content.replace("\"", "\\\"") + "\"")
        return record
    }

    private fun createNlipSubmessageRecord(
        requestUuid: UUID,
        format: JooqAllowedFormat,
        subformat: String,
        content: String,
        label: String?
    ): io.availe.jooq.tables.records.NlipSubmessageRecord {
        val record = io.availe.jooq.tables.records.NlipSubmessageRecord()
        record.requestUuid = requestUuid
        record.format = format
        record.subformat = subformat
        record.content = JSONB.valueOf("\"" + content.replace("\"", "\\\"") + "\"")
        record.label = label
        return record
    }
}
