package io.github.codymikol.kotlintest.execute.kotest

import io.github.codymikol.kotlintest.execute.RunReport
import io.github.codymikol.kotlintest.execute.TestResult
import io.github.codymikol.kotlintest.execute.TestStatus
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.listeners.IgnoredTestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestType
import io.kotest.engine.listener.TestEngineListener
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import io.kotest.engine.test.TestResult as KotestTestResult

/**
 * Implements Kotest's [TestEngineListener] for the sole purpose of observing spec/test completion
 * to create a [report].
 */
internal class KotestTestReporter : IgnoredTestListener, TestCaseExtension {
    private val mutex = Mutex()
    private val results: MutableSet<TestResult> = mutableSetOf()

    internal fun report(): RunReport = this.results.toSet()

    override suspend fun ignoredTest(testCase: TestCase, reason: String?) {
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

    override suspend fun intercept(
        testCase: TestCase,
        execute: suspend (TestCase) -> KotestTestResult
    ): KotestTestResult {
        val result = execute(testCase)

        if (testCase.type == TestType.Container) {
            return result
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

        return result
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
