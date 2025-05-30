package io.availe.routes

import io.availe.models.BranchId
import io.availe.models.InternalMessage
import io.availe.models.Session
import io.availe.services.ChatError
import io.availe.services.ChatService
import io.availe.services.toApiError
import io.availe.services.toStatusCode
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionRequest(val session: Session)

@Serializable
data class SendMessageRequest(val branchId: String = "root", val message: InternalMessage)

@Serializable
data class EditMessageRequest(
    val branchId: String = "root",
    val message: InternalMessage,
    val forkBranch: Boolean = false
)

@Serializable
data class EditMessageResponse(val branchId: String)

@Serializable
data class DeleteMessageRequest(
    val branchId: String = "root",
    val messageId: String,
    val updateTimestamp: Long = System.currentTimeMillis()
)

@Serializable
data class SessionListResponse(val sessionIds: List<String>)

@Serializable
data class BranchSnapshotResponse(val branches: Map<String, List<InternalMessage>>)

@Serializable
data class UpdateSessionTitleRequest(val title: String)

fun Route.chatServiceRoutes() = route("/api/chat/sessions") {

    get {
        val ids = ChatService.getAllSessionIdentifiers()
        call.respond(SessionListResponse(ids))
    }

    post {
        val req = call.receive<CreateSessionRequest>()
        ChatService.createSession(req.session).fold(
            { err -> call.respond(err.toStatusCode(), err.toApiError()) },
            { call.respond(HttpStatusCode.Created, mapOf("message" to "Session created")) }
        )
    }

    route("/{sessionId}") {
        get {
            val id = call.parameters["sessionId"]!!
            ChatService.getSession(id).fold(
                { err -> call.respond(err.toStatusCode(), err.toApiError()) },
                { call.respond(it) }
            )
        }

        delete {
            val id = call.parameters["sessionId"]!!
            ChatService.deleteSession(id).fold(
                { err -> call.respond(err.toStatusCode(), err.toApiError()) },
                { call.respond(HttpStatusCode.NoContent) }
            )
        }

        // Endpoint to update session title
        put("/title") {
            val id = call.parameters["sessionId"]!!
            val req = call.receive<UpdateSessionTitleRequest>()
            ChatService.updateSessionTitle(id, req.title).fold(
                { err -> call.respond(err.toStatusCode(), err.toApiError()) },
                { call.respond(HttpStatusCode.OK, mapOf("message" to "Session title updated")) }
            )
        }

        route("/messages") {
            get {
                val id = call.parameters["sessionId"]!!
                ChatService.getBranchSnapshot(id).fold(
                    { err -> call.respond(err.toStatusCode(), err.toApiError()) },
                    { branches ->
                        call.respond(
                            BranchSnapshotResponse(branches.mapKeys { it.key.value })
                        )
                    }
                )
            }

            put {
                val id = call.parameters["sessionId"]!!
                val req = call.receive<EditMessageRequest>()
                ChatService.editMessage(id, BranchId(req.branchId), req.message, req.forkBranch).fold(
                    { err -> call.respond(err.toStatusCode(), err.toApiError()) },
                    { newBranchId -> call.respond(EditMessageResponse(branchId = newBranchId.value)) }
                )
            }

            delete {
                val id = call.parameters["sessionId"]!!
                val req = call.receive<DeleteMessageRequest>()
                ChatService.deleteMessage(id, BranchId(req.branchId), req.messageId, req.updateTimestamp).fold(
                    { err -> call.respond(err.toStatusCode(), err.toApiError()) },
                    { call.respond(HttpStatusCode.NoContent) }
                )
            }
        }

        route("/branches/{branchId}/messages") {
            get {
                val id = call.parameters["sessionId"]!!
                val branch = call.parameters["branchId"]!!
                ChatService.getBranchSnapshot(id).fold(
                    { err -> call.respond(err.toStatusCode(), err.toApiError()) },
                    { snap ->
                        snap[BranchId(branch)]
                            ?.let { call.respond(it) }
                            ?: call.respond(
                                HttpStatusCode.NotFound,
                                ChatError.BranchNotFound(id, branch).toApiError()
                            )
                    }
                )
            }
        }
    }
}
