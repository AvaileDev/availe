package io.availe.mappers

import io.availe.openapi.model.NLIPRequest
import io.availe.openapi.model.NLIPSubMessage
import io.availe.openapi.model.AllowedFormat
import io.availe.jooq.tables.records.NlipRequestRecord
import io.availe.jooq.tables.records.NlipSubmessageRecord
import org.jooq.JSONB
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mapstruct.factory.Mappers

/**
 * Tests for the NlipRequestMapper.
 * Verifies that the mapper correctly converts between DTOs and entities.
 */
class NLIPRequestMapperTest {

    private val mapper: NlipRequestMapper = Mappers.getMapper(NlipRequestMapper::class.java)
    private val submessageMapper: NlipSubMessageMapper = Mappers.getMapper(NlipSubMessageMapper::class.java)

    @Test
    fun `test toEntity converts DTO to entity correctly`() {
        // Arrange
        val dto = NLIPRequest(
            format = AllowedFormat.text,
            subformat = "plain",
            content = "Hello, world!",
            messagetype = NLIPRequest.Messagetype.control,
            submessages = listOf(
                NLIPSubMessage(
                    format = AllowedFormat.token,
                    subformat = "correlator",
                    content = "123456",
                    label = "test-label"
                )
            )
        )

        // Act
        val entity = mapper.toEntity(dto)

        // Assert
        assertEquals(AllowedFormat.text.name, entity.format.name)
        assertEquals("plain", entity.subformat)
        assertEquals("Hello, world!", entity.content.data())
        assertEquals(NLIPRequest.Messagetype.control.name, entity.messagetype.name)
    }

    @Test
    fun `test toSubEntities converts submessages correctly`() {
        // Arrange
        val submessages = listOf(
            NLIPSubMessage(
                format = AllowedFormat.token,
                subformat = "correlator",
                content = "123456",
                label = "test-label"
            ),
            NLIPSubMessage(
                format = AllowedFormat.structured,
                subformat = "json",
                content = """{"key": "value"}""",
                label = null
            )
        )

        // Act
        val entities = mapper.toSubEntities(submessages)

        // Assert
        assertEquals(2, entities.size)
        
        assertEquals(AllowedFormat.token.name, entities[0].format.name)
        assertEquals("correlator", entities[0].subformat)
        assertEquals("123456", entities[0].content.data())
        assertEquals("test-label", entities[0].label)
        
        assertEquals(AllowedFormat.structured.name, entities[1].format.name)
        assertEquals("json", entities[1].subformat)
        assertEquals("""{"key": "value"}""", entities[1].content.data())
        assertNull(entities[1].label)
    }

    @Test
    fun `test fromEntity converts entity to DTO correctly`() {
        // Arrange
        val parent = NlipRequestRecord()
        parent.format = io.availe.jooq.enums.AllowedFormat.text
        parent.subformat = "plain"
        parent.content = JSONB.valueOf("Hello, world!")
        parent.messagetype = io.availe.jooq.enums.MessageType.control

        val child1 = NlipSubmessageRecord()
        child1.format = io.availe.jooq.enums.AllowedFormat.token
        child1.subformat = "correlator"
        child1.content = JSONB.valueOf("123456")
        child1.label = "test-label"

        val child2 = NlipSubmessageRecord()
        child2.format = io.availe.jooq.enums.AllowedFormat.structured
        child2.subformat = "json"
        child2.content = JSONB.valueOf("""{"key": "value"}""")
        child2.label = null

        val children = listOf(child1, child2)

        // Act
        val dto = mapper.fromEntity(parent, children)

        // Assert
        assertEquals(AllowedFormat.text, dto.format)
        assertEquals("plain", dto.subformat)
        assertEquals("Hello, world!", dto.content)
        assertEquals(NLIPRequest.Messagetype.control, dto.messagetype)
        assertEquals(2, dto.submessages?.size)
        
        assertEquals(AllowedFormat.token, dto.submessages?.get(0)?.format)
        assertEquals("correlator", dto.submessages?.get(0)?.subformat)
        assertEquals("123456", dto.submessages?.get(0)?.content)
        assertEquals("test-label", dto.submessages?.get(0)?.label)
        
        assertEquals(AllowedFormat.structured, dto.submessages?.get(1)?.format)
        assertEquals("json", dto.submessages?.get(1)?.subformat)
        assertEquals("""{"key": "value"}""", dto.submessages?.get(1)?.content)
        assertNull(dto.submessages?.get(1)?.label)
    }

    @Test
    fun `test null handling in toEntity`() {
        // Arrange
        val dto = NLIPRequest(
            format = AllowedFormat.text,
            subformat = "plain",
            content = "Hello, world!",
            messagetype = null,
            submessages = null
        )

        // Act
        val entity = mapper.toEntity(dto)

        // Assert
        assertEquals(AllowedFormat.text.name, entity.format.name)
        assertEquals("plain", entity.subformat)
        assertEquals("Hello, world!", entity.content.data())
        assertNull(entity.messagetype)
    }

    @Test
    fun `test null handling in fromEntity`() {
        // Arrange
        val parent = NlipRequestRecord()
        parent.format = io.availe.jooq.enums.AllowedFormat.text
        parent.subformat = "plain"
        parent.content = JSONB.valueOf("Hello, world!")
        parent.messagetype = null

        // Act
        val dto = mapper.fromEntity(parent, emptyList())

        // Assert
        assertEquals(AllowedFormat.text, dto.format)
        assertEquals("plain", dto.subformat)
        assertEquals("Hello, world!", dto.content)
        assertNull(dto.messagetype)
        assertEquals(0, dto.submessages?.size)
    }
}