package io.github.codymikol.kotlintest.execute.kotest

import io.github.codymikol.kotlintest.execute.RunReport
import io.github.codymikol.kotlintest.execute.TestResult
import io.github.codymikol.kotlintest.execute.TestStatus
import io.kotest.common.KotestInternal
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestType
import io.kotest.engine.listener.AbstractTestEngineListener
import io.kotest.engine.listener.TestEngineListener
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import io.kotest.engine.test.TestResult as KotestTestResult

/**
 * Implements Kotest's [TestEngineListener] for the sole purpose of observing spec/test completion
 * to create a [report].
 */
@OptIn(KotestInternal::class)
internal class KotestTestReporter : AbstractTestEngineListener() {
    private val mutex = Mutex()
    private val results: MutableSet<TestResult> = mutableSetOf()

    internal fun report(): RunReport = this.results.toSet()

    /**
     * Invoked if a [TestCase] will be skipped.
     */
    override suspend fun testIgnored(
        testCase: TestCase,
        reason: String?,
    ) {
        if (testCase.type == TestType.Container) {
            return
        }

        mutex.withLock {
            results.add(
                TestResult(
                    className = checkNotNull(testCase.spec.javaClass.kotlin.qualifiedName),
                    status = TestStatus.Ignored(reason = reason),
                    duration = Duration.ZERO,
                    id = testCase.toId(),
                ),
            )
        }
    }

    /**
     * Invoked when all the invocations of a [TestCase] have completed.
     * This function will only be invoked if a test case was enabled.
     */
    override suspend fun testFinished(
        testCase: TestCase,
        result: KotestTestResult,
    ) {
        if (testCase.type == TestType.Container) {
            return
        }

        mutex.withLock {
            results.add(
                TestResult(
                    className = checkNotNull(testCase.spec.javaClass.kotlin.qualifiedName),
                    status = TestStatus.from(result),
                    duration = result.duration,
                    id = testCase.toId(),
                ),
            )
        }
    }
}

internal fun TestCase.toId(): String {
    var testCase: TestCase? = this

    return buildList {
        while (testCase != null) {
            this.add(testCase.name.name)
            testCase = testCase.parent
        }
    }.reversed().joinToString(separator = "::")
}
