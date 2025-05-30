package io.availe.routes

import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Route.staticRoutes() {
    staticResources("/static", basePackage = "static")
}
