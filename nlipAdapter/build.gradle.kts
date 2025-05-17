plugins {
    alias(libs.plugins.kotlinJvm)
}


kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(projects.shared)
}