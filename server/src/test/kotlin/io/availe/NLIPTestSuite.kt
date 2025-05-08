package io.availe

import io.availe.mappers.NLIPRequestMapperTest
import io.availe.repositories.NLIPRequestRepositoryTest
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

/**
 * Test suite that runs all NLIP-related tests.
 */
@RunWith(Suite::class)
@SuiteClasses(
    NLIPRequestMapperTest::class,
    NLIPRequestRepositoryTest::class
)
class NLIPTestSuite