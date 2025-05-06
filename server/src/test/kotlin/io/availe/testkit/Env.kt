package io.nvelo.testkit

import java.io.File

object Env {
    private val raw: Map<String, String> by lazy { loadEnvFile() + System.getenv() }

    val dbUrl by lazy { raw["DB_URL"] ?: error("DB_URL missing") }
    val dbUser by lazy { raw["DB_USER"] ?: error("DB_USER missing") }
    val dbPass by lazy { raw["DB_PASS"] ?: error("DB_PASS missing") }
    val persist by lazy { raw["TEST_PERSIST"]?.toBoolean() ?: false }

    private fun loadEnvFile(name: String = ".env"): Map<String, String> =
        File(name).takeIf { it.exists() }?.useLines { lines ->
            lines.filter { "=" in it && !it.trim().startsWith('#') }
                .associate { it.substringBefore('=').trim() to it.substringAfter('=').trim() }
        } ?: emptyMap()
}
