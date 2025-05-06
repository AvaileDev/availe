package io.availe.repositories

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    SaveAndFindRequestTest::class,
    SaveAndFindRequestWithSubmessagesTest::class,
    FindNonExistentRequestTest::class
)
class NLIPRequestRepositoryTestSuite