package io.availe.routes

import io.ktor.http.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.staticRoutes() {
    staticResources("/static", basePackage = "static")

    get("/") {
        val html = this::class.java
            .classLoader
            .getResource("index.html")
            ?.readText()
            ?: error("index.html not found on classpath")
        call.respondText(html, ContentType.Text.Html)
    }
}
