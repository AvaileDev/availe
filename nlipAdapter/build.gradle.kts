plugins {
    alias(libs.plugins.kotlinJvm)
    application
}


kotlin {
    jvmToolchain(21)
}

group = "io.availe"
version = "1.0.0"
application {
    mainClass.set("io.availe.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}


dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)


    implementation(libs.mapstruct)

    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.logback)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.server.content.negotiation)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.postgresql)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.database.postgresql)
    testImplementation(libs.snakeyaml)
    testImplementation(platform(libs.jackson.bom))
    testImplementation(libs.jackson.databind)
}