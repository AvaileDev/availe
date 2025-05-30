package io.availe.services

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.right
import io.availe.models.BranchId
import io.availe.models.InternalMessage
import io.availe.models.Session
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class InMemorySessionStore(
    private val maximumSessions: Int = 1000,
    private val maximumBranchesPerSession: Int = 1000,
    private val maximumMessagesPerBranch: Int = 1000
) {
    private val storeMutex = Mutex()
    private val data: ConcurrentHashMap<String, SessionState> = ConcurrentHashMap()

    internal data class SessionState(
        val lock: Mutex = Mutex(),
        var session: Session,
        var branches: PersistentMap<BranchId, PersistentMap<String, InternalMessage>>,
        val maximumBranches: Int,
        val maximumMessagesPerBranch: Int
    ) {
        fun send(
            branchIdentifier: BranchId,
            message: InternalMessage
        ): Either<ChatError, Unit> = either {
            val messages =
                branches[branchIdentifier] ?: raise(ChatError.BranchNotFound(session.id, branchIdentifier.value))
            ensure(messages.size < maximumMessagesPerBranch) {
                ChatError.BranchMessageLimitExceeded(session.id, branchIdentifier.value)
            }
            ensure(!messages.containsKey(message.id)) {
                ChatError.MessageAlreadyExists(session.id, message.id)
            }
            val newMessages = messages.put(message.id, message)
            branches = branches.put(branchIdentifier, newMessages)
            session = session.copy(lastActivityAt = message.timeStamp)
        }

        fun edit(
            branchIdentifier: BranchId,
            message: InternalMessage,
            forkBranch: Boolean
        ): Either<ChatError, BranchId> = either {
            val messages =
                branches[branchIdentifier] ?: raise(ChatError.BranchNotFound(session.id, branchIdentifier.value))
            ensure(messages.containsKey(message.id)) {
                ChatError.MessageNotFound(session.id, message.id)
            }
            if (forkBranch) {
                ensure(branches.size < maximumBranches) {
                    ChatError.BranchLimitExceeded(session.id)
                }
                var cloned = persistentMapOf<String, InternalMessage>()
                for ((key, value) in messages) {
                    if (key == message.id) {
                        cloned = cloned.put(key, message)
                        break
                    }
                    cloned = cloned.put(key, value)
                }
                val newBranchIdentifier = BranchId.random()
                branches = branches.put(newBranchIdentifier, cloned)
                session = session.copy(lastActivityAt = message.timeStamp)
                newBranchIdentifier
            } else {
                val newMessages = messages.put(message.id, message)
                branches = branches.put(branchIdentifier, newMessages)
                session = session.copy(lastActivityAt = message.timeStamp)
                branchIdentifier
            }
        }

        fun delete(
            branchIdentifier: BranchId,
            messageId: String,
            updateTimestamp: Long
        ): Either<ChatError, Unit> = either {
            val messages =
                branches[branchIdentifier] ?: raise(ChatError.BranchNotFound(session.id, branchIdentifier.value))
            ensure(messages.containsKey(messageId)) {
                ChatError.MessageNotFound(session.id, messageId)
            }
            val newMessages = messages.remove(messageId)
            branches = branches.put(branchIdentifier, newMessages)
            session = session.copy(lastActivityAt = updateTimestamp)
        }
    }

    suspend fun tryInsert(session: Session): Either<ChatError, Unit> = storeMutex.withLock {
        either {
            ensure(data.size < maximumSessions) {
                ChatError.SessionLimitExceeded(session.id)
            }
            ensure(
                data.putIfAbsent(
                    session.id,
                    SessionState(
                        session = session,
                        branches = persistentMapOf(BranchId.root to persistentMapOf()),
                        maximumBranches = maximumBranchesPerSession,
                        maximumMessagesPerBranch = maximumMessagesPerBranch
                    )
                ) == null
            ) {
                ChatError.SessionAlreadyExists(session.id)
            }
        }
    }

    suspend fun delete(sessionId: String): Either<ChatError, Unit> = storeMutex.withLock {
        if (data.remove(sessionId) != null) {
            Either.Right(Unit)
        } else {
            ChatError.SessionNotFound(sessionId).left()
        }
    }

    internal suspend fun <R> mutateSession(
        sessionId: String,
        block: suspend SessionState.() -> Either<ChatError, R>
    ): Either<ChatError, R> = storeMutex.withLock {
        either {
            val state = data[sessionId] ?: raise(ChatError.SessionNotFound(sessionId))
            state.lock.withLock {
                block(state).bind()
            }
        }
    }

    suspend fun snapshotBranches(sessionId: String): Either<ChatError, Map<BranchId, List<InternalMessage>>> =
        storeMutex.withLock {
            either {
                val state = data[sessionId] ?: raise(ChatError.SessionNotFound(sessionId))
                state.lock.withLock {
                    state.branches.mapValues { (_, map) -> map.values.toList() }
                }
            }
        }

    fun get(sessionId: String): Either<ChatError, Session> =
        data[sessionId]?.session?.right() ?: ChatError.SessionNotFound(sessionId).left()

    fun getAllSessionIds(): List<String> = data.keys.toList()
}
