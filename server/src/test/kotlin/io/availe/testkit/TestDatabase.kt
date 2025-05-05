package io.availe.testkit

import java.sql.Connection
import java.sql.DriverManager
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL

class TestDatabase {
    private val conn: Connection = DriverManager.getConnection(
        Env.dbUrl, Env.dbUser, Env.dbPass
    )

    val dsl: DSLContext = DSL.using(conn, SQLDialect.POSTGRES)

    fun <R> tx(block: DSLContext.() -> R): R =
        if (Env.persist) dsl.block()
        else dsl.transactionResult { cfg -> DSL.using(cfg).block() }

    fun cleanup() {
        if (!conn.isClosed) conn.close()
    }
}