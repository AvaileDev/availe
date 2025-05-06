package io.nvelo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform