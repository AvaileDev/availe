package io.availe.mappers

import io.availe.openapi.model.NLIPSubMessage
import io.availe.openapi.model.AllowedFormat
import io.availe.jooq.tables.records.NlipSubmessageRecord
import org.jooq.JSONB
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mapstruct.factory.Mappers

/**
 * Tests for the NlipSubMessageMapper.
 * Verifies that the mapper correctly converts between DTOs and entities.
 */
class NlipSubMessageMapperTest {

    private val mapper: NlipSubMessageMapper = Mappers.getMapper(NlipSubMessageMapper::class.java)

    @Test
    fun `test toEntity converts DTO to entity correctly`() {
        // Arrange
        val dto = NLIPSubMessage(
            format = AllowedFormat.token,
            subformat = "correlator",
            content = "123456",
            label = "test-label"
        )

        // Act
        val entity = mapper.toEntity(dto)

        // Assert
        assertEquals(AllowedFormat.token.name, entity.format.name)
        assertEquals("correlator", entity.subformat)
        assertEquals("123456", entity.content.data())
        assertEquals("test-label", entity.label)
    }

    @Test
    fun `test fromEntity converts entity to DTO correctly`() {
        // Arrange
        val entity = NlipSubmessageRecord()
        entity.format = io.availe.jooq.enums.AllowedFormat.structured
        entity.subformat = "json"
        entity.content = JSONB.valueOf("""{"key": "value"}""")
        entity.label = "test-label"

        // Act
        val dto = mapper.fromEntity(entity)

        // Assert
        assertEquals(AllowedFormat.structured, dto.format)
        assertEquals("json", dto.subformat)
        assertEquals("""{"key": "value"}""", dto.content)
        assertEquals("test-label", dto.label)
    }

    @Test
    fun `test null handling in toEntity`() {
        // Arrange
        val dto = NLIPSubMessage(
            format = AllowedFormat.text,
            subformat = "plain",
            content = "Hello, world!",
            label = null
        )

        // Act
        val entity = mapper.toEntity(dto)

        // Assert
        assertEquals(AllowedFormat.text.name, entity.format.name)
        assertEquals("plain", entity.subformat)
        assertEquals("Hello, world!", entity.content.data())
        assertNull(entity.label)
    }

    @Test
    fun `test null handling in fromEntity`() {
        // Arrange
        val entity = NlipSubmessageRecord()
        entity.format = io.availe.jooq.enums.AllowedFormat.text
        entity.subformat = "plain"
        entity.content = JSONB.valueOf("Hello, world!")
        entity.label = null

        // Act
        val dto = mapper.fromEntity(entity)

        // Assert
        assertEquals(AllowedFormat.text, dto.format)
        assertEquals("plain", dto.subformat)
        assertEquals("Hello, world!", dto.content)
        assertNull(dto.label)
    }
}