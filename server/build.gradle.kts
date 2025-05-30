import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.Logging


/** ------------- Load .env ------------- */

fun loadEnv(fileName: String = ".env"): Map<String, String> =
    file(fileName).takeIf { it.exists() }?.readLines()
        ?.filter { it.isNotBlank() && !it.startsWith("#") && "=" in it }
        ?.associate {
            val (key, value) = it.split("=", limit = 2)
            key.trim() to value.trim()
        }.orEmpty()

val env = loadEnv()
val dbUrl = env["DB_URL"] ?: error("Missing DB_URL in .env")
val dbUser = env["DB_USER"] ?: error("Missing DB_USER in .env")
val dbPass = env["DB_PASS"] ?: error("Missing DB_PASS in .env")
val dbSchema = env["DB_SCHEMA"] ?: "public"

/** ------------- Buildscript class‑path for Flyway ------------- */
buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath(libs.flyway.database.postgresql)
        classpath(libs.postgresql)
    }
}

/** ------------- Load plugins ------------- */

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jooq)
    alias(libs.plugins.flyway)
    alias(libs.plugins.shadow)
    id("org.jetbrains.kotlin.kapt")
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

    jooqGenerator(libs.postgresql)

    implementation(libs.mapstruct)
    kapt(libs.mapstruct.processor)

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
    implementation(libs.arrow.core)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.ktor.server.cors.jvm)
    implementation(libs.ktor.server.status.pages.jvm)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.postgresql)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.database.postgresql)
    testImplementation(libs.snakeyaml)
    testImplementation(platform(libs.jackson.bom))
    testImplementation(libs.jackson.databind)
}

/** ------------- jOOQ config ------------- */

jooq {
    edition.set(JooqEdition.OSS)
    configurations {
        create("main") {
            // run before compileKotlin
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                logging = Logging.WARN

                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = dbUrl
                    user = dbUser
                    password = dbPass
                }

                generator.apply {
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = dbSchema
                    }
                    generate.apply {
                        isImmutablePojos = true
                        isRecords = true
                    }
                    target.apply {
                        packageName = "io.availe.jooq"
                        directory = layout.buildDirectory
                            .dir("generated-src/jooq/main")
                            .get()
                            .asFile
                            .absolutePath
                    }
                }
            }
        }
    }
}

sourceSets["main"].java.srcDir(
    layout.buildDirectory.dir("generated-src/jooq/main")
)

/** ------------- Flyway config ------------- */

flyway {
    url = dbUrl
    user = dbUser
    password = dbPass
    schemas = arrayOf(dbSchema)
    locations = arrayOf("filesystem:${rootProject.layout.projectDirectory.asFile}/src/main/resources/db/migration")
    cleanDisabled = false
}

/** ------------- Build hooks & helper tasks ------------- */

// Always run Flyway before compiling Kotlin and generating jOOQ classes
tasks.named("compileKotlin") {
    dependsOn("flywayMigrate")
}

tasks.register<Test>("runEnumValuesSyncTest") {
    group = "verification"
    description = "Verifies that our enums match the OpenAPI schema"

    filter {
        includeTestsMatching("io.availe.drift.EnumValuesSyncTest")
    }

    val openApiFile = project(":shared")
        .projectDir
        .resolve("src/commonMain/resources/openapi/nlip-api.yaml")

    systemProperty("OPENAPI_SPEC_PATH", openApiFile.absolutePath)
}

// Always run Flyway before starting the server
tasks.named<JavaExec>("run") {
    dependsOn("flywayMigrate", "runEnumValuesSyncTest")
}


// Reset the database if needed
tasks.register("resetDb") {
    group = "database"
    description = "Drops all objects and re-applies all Flyway migrations"
    dependsOn("flywayClean", "flywayMigrate")
}

// Drop all objects from DB
tasks.register("clearDb") {
    group = "database"
    description = "Drops all objects"
    dependsOn("flywayClean")
}

// produce bundled jvm app for docker
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("server")
    archiveClassifier.set("all")
    archiveVersion.set("")
    mergeServiceFiles()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(project(":shared").tasks.named("openApiGenerate"))
}
