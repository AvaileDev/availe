package io.availe.repositories

import io.availe.testkit.BaseRepositoryTest
import io.availe.models.NLIPRequest
import io.availe.models.NLIPSubMessage
import io.availe.models.AllowedFormat
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
fun dummyRequest(id: Uuid = UUID.randomUUID().toKotlinUuid()) = NLIPRequest(
    control = false,
    format = AllowedFormat.text,
    subformat = "plain",
    content = JsonPrimitive("Hello, world!"),
    submessages = emptyList(),
    uuid = id
)

@OptIn(ExperimentalUuidApi::class)
class SaveAndFindRequestTest : BaseRepositoryTest() {
    @Test
    fun `saving a request then finding by UUID returns an identical object`() {
        db.tx {
            val repo = NLIPRequestRepository(this)
            val javaUuid = UUID.randomUUID()
            val kotlinUuid = javaUuid.toKotlinUuid()
            val request = dummyRequest(kotlinUuid)

            repo.save(request)
            val found = repo.find(javaUuid)!!

            assertNotNull(found)
            assertEquals(kotlinUuid, found.uuid)
            assertEquals(false, found.control)
            assertEquals(AllowedFormat.text, found.format)
            assertEquals("plain", found.subformat)
            assertEquals(JsonPrimitive("Hello, world!"), found.content)
            assertEquals(0, found.submessages.size)
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
class SaveAndFindRequestWithSubmessagesTest : BaseRepositoryTest() {
    @Test
    fun `saving and finding a structured request with submessages works`() {
        db.tx {
            val repo = NLIPRequestRepository(this)
            val javaUuid = UUID.randomUUID()
            val kotlinUuid = javaUuid.toKotlinUuid()
            val request = NLIPRequest(
                control = true,
                format = AllowedFormat.structured,
                subformat = "json",
                content = JsonPrimitive("Main content"),
                submessages = listOf(
                    NLIPSubMessage(
                        format = AllowedFormat.text,
                        subformat = "plain",
                        content = JsonPrimitive("Submessage 1")
                    ),
                    NLIPSubMessage(
                        format = AllowedFormat.token,
                        subformat = "embedding",
                        content = JsonPrimitive("Submessage 2")
                    )
                ),
                uuid = kotlinUuid
            )

            repo.save(request)
            val found = repo.find(javaUuid)!!

            assertNotNull(found)
            assertEquals(kotlinUuid, found.uuid)
            assertEquals(true, found.control)
            assertEquals(AllowedFormat.structured, found.format)
            assertEquals("json", found.subformat)
            assertEquals(JsonPrimitive("Main content"), found.content)
            assertEquals(2, found.submessages.size)

            assertEquals(AllowedFormat.text, found.submessages[0].format)
            assertEquals("plain", found.submessages[0].subformat)
            assertEquals(JsonPrimitive("Submessage 1"), found.submessages[0].content)

            assertEquals(AllowedFormat.token, found.submessages[1].format)
            assertEquals("embedding", found.submessages[1].subformat)
            assertEquals(JsonPrimitive("Submessage 2"), found.submessages[1].content)
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
