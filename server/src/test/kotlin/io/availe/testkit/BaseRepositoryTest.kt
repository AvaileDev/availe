package io.availe.testkit

import org.junit.Before
import org.junit.After

abstract class BaseRepositoryTest {
    protected lateinit var db: TestDatabase

    @Before
    fun setUp() {
        db = TestDatabase()
    }

    @After
    fun tearDown() {
        db.cleanup()
    }
}