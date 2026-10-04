package io.github.codymikol.kotlintest.execute.kotest

import io.github.codymikol.kotlintest.execute.RunReport
import io.github.codymikol.kotlintest.execute.TestResult
import io.github.codymikol.kotlintest.execute.TestStatus
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.listeners.IgnoredTestListener
import io.kotest.core.spec.SpecRef
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
internal class KotestTestReporter : IgnoredTestListener, TestCaseExtension {
    private val mutex = Mutex()
    private val results: MutableList<Pair<KotestPath, KotestResult>> = mutableListOf()

    /**
     * Every test and container seen, used to determine the siblings of a test when
     * translating Kotest's duplicate names, see [toDiscoveredName].
     */
    private val seen: MutableSet<KotestPath> = mutableSetOf()

    /**
     * Failures of entire specs, reported with an empty id, see [engineListener].
     */
    private val classResults: MutableSet<TestResult> = mutableSetOf()

    private data class KotestResult(
        val status: TestStatus,
        val duration: Duration,
    )

    internal fun report(): RunReport {
        val siblings = seen.groupBy({ it.dropLast(1) }, { it.last() }).mapValues { (_, names) -> names.toSet() }

        return results
            .map { (path, result) ->
                TestResult(
                    className = path.first(),
                    id = path.toDiscoveredId(siblings),
                    status = result.status,
                    duration = result.duration,
                )
            }.toSet() + classResults
    }

    /**
     * Observes failures of entire specs, which are not reported per test, e.g. an exception
     * in `beforeSpec` or a spec that fails to instantiate.
     */
    internal val engineListener: TestEngineListener =
        object : AbstractTestEngineListener() {
            override suspend fun specFinished(
                ref: SpecRef,
                result: KotestTestResult,
            ) {
                val error = result.errorOrNull ?: return
                val className = ref.kclass.qualifiedName ?: ref.kclass.java.name

                mutex.withLock {
                    classResults.add(
                        TestResult(
                            className = className,
                            id = "",
                            status = TestStatus.Failure.fromClassError(className, error),
                            duration = result.duration,
                        ),
                    )
                }
            }
        }

    /**
     * Records that [testCase] was seen and, unless it's a container, its [result].
     */
    private suspend fun record(testCase: TestCase, result: () -> KotestResult) {
        val path = testCase.toKotestPath()

        mutex.withLock {
            seen.add(path)

            if (testCase.type != TestType.Container) {
                results.add(path to result())
            }
        }
    }

    override suspend fun ignoredTest(testCase: TestCase, reason: String?) {
        record(testCase) { KotestResult(status = TestStatus.Ignored(reason = reason), duration = Duration.ZERO) }
    }

    override suspend fun intercept(
        testCase: TestCase,
        execute: suspend (TestCase) -> KotestTestResult
    ): KotestTestResult {
        val result = execute(testCase)

        record(testCase) { KotestResult(status = TestStatus.from(result), duration = result.duration) }

        return result
    }
}

/**
 * The fully qualified spec class name followed by the Kotest names of a [TestCase] and its parents.
 */
private typealias KotestPath = List<String>

private fun TestCase.toKotestPath(): KotestPath {
    var testCase: TestCase? = this

    return buildList {
        while (testCase != null) {
            this.add(testCase.name.name)
            testCase = testCase.parent
        }
        this.add(checkNotNull(this@toKotestPath.spec.javaClass.kotlin.qualifiedName))
    }.reversed()
}

/**
 * The '::' separated id as discovered, translating Kotest's duplicate names at every level.
 */
private fun KotestPath.toDiscoveredId(siblings: Map<KotestPath, Set<String>>): String =
    (1 until size).joinToString(separator = "::") { index ->
        val parent = subList(0, index)
        this[index].toDiscoveredName(siblings[parent].orEmpty())
    }
