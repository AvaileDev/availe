package io.availe.services

import arrow.core.Either
import io.availe.models.BranchId
import io.availe.models.InternalMessage
import io.availe.models.Session
import io.availe.openapi.model.AllowedFormat
import io.availe.openapi.model.NLIPRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatServiceConcurrencyTest {
    private lateinit var chatService: TestChatService

    @Before
    fun initialize() {
        chatService = TestChatService()
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    private fun createTestSession(id: String = Uuid.random().toString()): Session {
        val currentTimestamp = Clock.System.now().toEpochMilliseconds()
        return Session(
            id = id,
            title = "Test Session $id",
            createdAt = currentTimestamp,
            lastActivityAt = currentTimestamp,
            participantIds = setOf("user1", "user2"),
            status = Session.Status.ACTIVE
        )
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    private fun createTestMessage(
        sessionId: String,
        id: String = Uuid.random().toString(),
        content: String = "Test message $id"
    ): InternalMessage {
        val currentTimestamp = Clock.System.now().toEpochMilliseconds()
        return InternalMessage(
            id = id,
            sessionId = sessionId,
            senderId = "user1",
            senderRole = InternalMessage.Role.USER,
            nlipMessage = NLIPRequest(
                format = AllowedFormat.text,
                subformat = "plain",
                content = content
            ),
            timeStamp = currentTimestamp,
            status = InternalMessage.Status.SENT
        )
    }

    @Test
    fun testConcurrentMessageEditing() = runBlocking {
        val sessionId = "edit-test-session"
        chatService.createSession(createTestSession(sessionId))
        val numberOfMessages = 50
        val messageIdentifiers = List(numberOfMessages) { "edit-message-$it" }
        messageIdentifiers.forEach { identifier ->
            chatService.sendMessage(sessionId, BranchId.root, createTestMessage(sessionId, identifier))
        }
        val numberOfEditAttempts = 10
        val successCounter = AtomicInteger(0)
        val failureCounter = AtomicInteger(0)
        coroutineScope {
            messageIdentifiers.forEach { identifier ->
                repeat(numberOfEditAttempts) { attemptIndex ->
                    launch {
                        val newContent = "Edited content $identifier - attempt $attemptIndex"
                        val message = createTestMessage(sessionId, identifier, newContent)
                        val shouldFork = attemptIndex % 2 == 0
                        when (
                            chatService.editMessage(
                                sessionId,
                                BranchId.root,
                                message,
                                forkBranch = shouldFork
                            )
                        ) {
                            is Either.Right -> successCounter.incrementAndGet()
                            is Either.Left -> failureCounter.incrementAndGet()
                        }
                    }
                }
            }
        }
        assertEquals(numberOfMessages * numberOfEditAttempts, successCounter.get() + failureCounter.get())
        assertEquals(0, failureCounter.get())

        val snapshot = chatService.getBranchSnapshot(sessionId).getOrNull()!!
        val rootMessages = snapshot[BranchId.root]!!
        assertEquals(numberOfMessages, rootMessages.size)
        assertEquals(numberOfMessages, rootMessages.map { it.id }.distinct().size)
    }

    @Test
    fun testEditWithForkBranchAndPreservesOriginalBranch() = runBlocking {
        val sessionId = "fork-test-session"
        chatService.createSession(createTestSession(sessionId))
        val messageA = createTestMessage(sessionId, "A")
        val messageB = createTestMessage(sessionId, "B")
        val messageC = createTestMessage(sessionId, "C")
        chatService.sendMessage(sessionId, BranchId.root, messageA)
        chatService.sendMessage(sessionId, BranchId.root, messageB)
        chatService.sendMessage(sessionId, BranchId.root, messageC)

        val editedMessageB = messageB.copy(nlipMessage = messageB.nlipMessage.copy(content = "B – edited"))
        val forkIdentifier = chatService.editMessage(
            sessionId,
            BranchId.root,
            editedMessageB,
            forkBranch = true
        ).getOrNull()!!

        val deleteCResult = chatService.deleteMessage(
            sessionId,
            BranchId.root,
            messageC.id,
            messageC.timeStamp
        )
        assertTrue(deleteCResult.isRight())

        val editedMessageBAgain =
            editedMessageB.copy(nlipMessage = messageB.nlipMessage.copy(content = "B – edited again"))
        val editResult = chatService.editMessage(sessionId, BranchId.root, editedMessageBAgain)
        assertTrue(editResult.isRight())

        val snapshot = chatService.getBranchSnapshot(sessionId).getOrNull()!!
        val rootIds = snapshot[BranchId.root]!!.map { it.id }
        val forkIds = snapshot[forkIdentifier]!!.map { it.id }

        assertEquals(listOf("A", "B"), rootIds)
        assertEquals(listOf("A", "B"), forkIds)

        val session = chatService.getSession(sessionId).getOrNull()!!
        assertEquals(editedMessageBAgain.timeStamp, session.lastActivityAt)
    }
}
