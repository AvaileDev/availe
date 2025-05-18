package io.availe.drift

import io.availe.testkit.Env
import io.availe.testkit.TestDatabase
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.yaml.snakeyaml.Yaml

class EnumValuesSyncTest {

    private lateinit var db: TestDatabase
    private lateinit var dsl: DSLContext
    private lateinit var yamlMap: Map<*, *>

    @Before
    fun setUp() {
        db = TestDatabase()
        Flyway.configure()
            .dataSource(Env.dbUrl, Env.dbUser, Env.dbPass)
            .load()
            .migrate()
        dsl = db.dsl

        val stream = javaClass.classLoader
            .getResourceAsStream("openapi/nlip-api.yaml")
            ?: error("openapi/nlip-api.yaml not found on classpath")

        yamlMap = Yaml().load<Any>(stream) as? Map<*, *>
            ?: error("Failed to parse OpenAPI YAML")
    }

    @After
    fun tearDown() {
        db.cleanup()
    }

    @Test
    fun `allowed_format enum values should match OpenAPI schema`() {
        val dbValues = getPostgresEnumValues("allowed_format")
        val schemaValues = getOpenApiEnumValues("AllowedFormat")
        assertEquals(
            "Enum mismatch for 'allowed_format'. OpenAPI: $schemaValues, DB: $dbValues",
            schemaValues.sorted(),
            dbValues.sorted()
        )
    }

    @Test
    fun `message_type enum values should match OpenAPI schema`() {
        val dbValues = getPostgresEnumValues("message_type")
        val schemaValues = getOpenApiEnumValues("NLIPRequest", "messagetype")
        assertEquals(
            "Enum mismatch for 'message_type'. OpenAPI: $schemaValues, DB: $dbValues",
            schemaValues.sorted(),
            dbValues.sorted()
        )
    }

    private fun getPostgresEnumValues(enumName: String): List<String> =
        dsl.fetch(
            """
            SELECT enumlabel
              FROM pg_enum
             WHERE enumtypid = '$enumName'::regtype
             ORDER BY enumsortorder
            """.trimIndent()
        ).map { it.get("enumlabel", String::class.java) }

    private fun getOpenApiEnumValues(
        enumSchemaName: String,
        propertyName: String? = null
    ): List<String> {
        val components = yamlMap["components"] as? Map<*, *>
            ?: error("Missing 'components' in OpenAPI spec")
        val schemas = components["schemas"] as? Map<*, *>
            ?: error("Missing 'schemas' in OpenAPI spec")

        return if (propertyName == null) {
            val enumSchema = schemas[enumSchemaName] as? Map<*, *>
                ?: error("Missing schema '$enumSchemaName'")
            val enumList = enumSchema["enum"] as? List<*>
                ?: error("Missing 'enum' in schema '$enumSchemaName'")
            enumList.map { it.toString() }
        } else {
            val schema = schemas[enumSchemaName] as? Map<*, *>
                ?: error("Missing schema '$enumSchemaName'")
            val properties = schema["properties"] as? Map<*, *>
                ?: error("Missing 'properties' in schema '$enumSchemaName'")
            val prop = properties[propertyName] as? Map<*, *>
                ?: error("Missing property '$propertyName' in schema '$enumSchemaName'")
            val enumList = prop["enum"] as? List<*>
                ?: error("Missing 'enum' in property '$propertyName' of schema '$enumSchemaName'")
            enumList.map { it.toString() }
        }
    }
}
