package io.availe.repositories

import io.availe.testkit.BaseRepositoryTest
import io.availe.models.createNLIPContent
import io.availe.openapi.model.AllowedFormat
import io.availe.openapi.model.NLIPRequest
import io.availe.openapi.model.NLIPSubMessage
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import java.util.UUID

// Helper function to create a simple request for testing
fun createSimpleRequest(uuid: UUID = UUID.randomUUID()) = NLIPRequest(
    uuid = uuid,
    format = AllowedFormat.text,
    subformat = "plain",
    content = createNLIPContent(JsonPrimitive("Hello, world!")),
    messagetype = null,
    submessages = null
)

class SaveAndFindRequestTest : BaseRepositoryTest() {
    @Test
    fun `saving a request then finding by UUID returns the same request`() {
        db.tx {
            val repo = NLIPRequestRepository(this)
            val uuid = UUID.randomUUID()
            val request = createSimpleRequest(uuid)

            repo.save(request)
            val found = repo.find(uuid)

            assertNotNull(found)
            assertEquals(uuid, found.uuid)
        }
    }
}

class SaveAndFindRequestWithSubmessagesTest : BaseRepositoryTest() {
    @Test
    fun `saving and finding a request with submessages works`() {
        db.tx {
            val repo = NLIPRequestRepository(this)
            val uuid = UUID.randomUUID()

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
                content = createNLIPContent(JsonPrimitive("Main content")),
                submessages = listOf(submessage1, submessage2)
            )

            repo.save(request)
            val found = repo.find(uuid)

            assertNotNull(found)
            assertEquals(uuid, found.uuid)
            assertEquals(2, found.submessages?.size)
        }
    }
}

class FindNonExistentRequestTest : BaseRepositoryTest() {
    @Test
    fun `finding a non-existent UUID returns null`() {
        db.tx {
            val repo = NLIPRequestRepository(this)
            val nonExistent = UUID.randomUUID()
            val found = repo.find(nonExistent)
            assertNull(found)
        }
    }
}
