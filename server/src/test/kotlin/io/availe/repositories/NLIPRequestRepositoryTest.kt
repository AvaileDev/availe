package io.availe.repositories

import io.availe.openapi.model.AllowedFormat
import io.availe.openapi.model.NLIPRequest
import io.availe.openapi.model.NLIPSubMessage
import io.availe.testkit.BaseRepositoryTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class NLIPRequestRepositoryTest : BaseRepositoryTest() {

    private lateinit var repository: NLIPRequestRepository

    @org.junit.Before
    fun initRepository() {
        repository = NLIPRequestRepository(db.dsl)
    }

    @Test
    fun `test save and find request with submessages`() {
        // Arrange
        val uuid = UUID.randomUUID()
        val request = NLIPRequest(
            uuid = uuid,
            messagetype = NLIPRequest.Messagetype.control,
            format = AllowedFormat.text,
            subformat = "plain",
            content = "Hello, world!",
            submessages = listOf(
                NLIPSubMessage(
                    format = AllowedFormat.structured,
                    subformat = "json",
                    content = """{"greeting": "Hello"}""",
                    label = "metadata"
                ),
                NLIPSubMessage(
                    format = AllowedFormat.binary,
                    subformat = "image",
                    content = "base64data",
                    label = null
                )
            )
        )

        // Act
        db.tx {
            repository.save(request)
        }
        val result = repository.find(uuid)

        // Assert
        assertEquals(uuid, result?.uuid)
        assertEquals(NLIPRequest.Messagetype.control, result?.messagetype)
        assertEquals(AllowedFormat.text, result?.format)
        assertEquals("plain", result?.subformat)
        assertEquals("Hello, world!", result?.content)

        assertEquals(2, result?.submessages?.size)

        val firstSubmessage = result?.submessages?.get(0)
        assertEquals(AllowedFormat.structured, firstSubmessage?.format)
        assertEquals("json", firstSubmessage?.subformat)
        assertEquals("""{"greeting": "Hello"}""", firstSubmessage?.content)
        assertEquals("metadata", firstSubmessage?.label)

        val secondSubmessage = result?.submessages?.get(1)
        assertEquals(AllowedFormat.binary, secondSubmessage?.format)
        assertEquals("image", secondSubmessage?.subformat)
        assertEquals("base64data", secondSubmessage?.content)
        assertNull(secondSubmessage?.label)
    }

    @Test
    fun `test save and find request without submessages`() {
        // Arrange
        val uuid = UUID.randomUUID()
        val request = NLIPRequest(
            uuid = uuid,
            messagetype = null,
            format = AllowedFormat.token,
            subformat = "embedding",
            content = "token data",
            submessages = null
        )

        // Act
        db.tx {
            repository.save(request)
        }
        val result = repository.find(uuid)

        // Assert
        assertEquals(uuid, result?.uuid)
        assertNull(result?.messagetype)
        assertEquals(AllowedFormat.token, result?.format)
        assertEquals("embedding", result?.subformat)
        assertEquals("token data", result?.content)
        assertEquals(0, result?.submessages?.size)
    }

    @Test
    fun `test find non-existent request`() {
        // Arrange
        val uuid = UUID.randomUUID()

        // Act
        val result = repository.find(uuid)

        // Assert
        assertNull(result)
    }
}
