package io.availe.routes

import io.availe.client.NLIPClient
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthRoutes(externalChat: NLIPClient) {
    get("/nlip/ping") {
        val reply = externalChat.ask("Ping NLIP server!")
        call.respondText("NLIP replied: ${reply.content}")
    }
}
