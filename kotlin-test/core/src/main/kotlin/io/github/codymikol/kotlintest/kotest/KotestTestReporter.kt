package io.github.codymikol.kotlintest.kotest

import io.github.codymikol.kotlintest.RunReport
import io.github.codymikol.kotlintest.TestNode
import io.github.codymikol.kotlintest.TestStatus
import io.kotest.common.KotestInternal
import io.kotest.core.spec.SpecRef
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestType
import io.kotest.engine.listener.AbstractTestEngineListener
import io.kotest.engine.listener.TestEngineListener
import io.kotest.engine.test.TestResult
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Implements Kotest's [TestEngineListener] for the sole purpose of observing spec/test completion
 * to create a [report].
 *
 * This reporter is **not** thread safe.
 */
@OptIn(KotestInternal::class)
internal class KotestTestReporter : AbstractTestEngineListener() {
    private val results: MutableSet<TestNode.Container> = mutableSetOf()

    internal fun report(): RunReport = this.results.toSet()

    /**
     * Invoked once per [SpecRef] to indicate that this spec will be instantiated
     * and any active tests invoked.
     */
    override suspend fun specStarted(ref: SpecRef) {
        val name = checkNotNull(ref.kclass.qualifiedName)
        this.results.add(TestNode.Container(name = name))
    }

    /**
     * Invoked when a spec is ignored. An optional [reason] for being ignored can be provided.
     */
    override suspend fun specIgnored(
        kclass: KClass<*>,
        reason: String?,
    ) {
        val name = checkNotNull(kclass.qualifiedName)
        this.results.add(TestNode.Container(name = name))
    }

    /**
     * Is invoked once per [SpecRef] class to indicate this spec has completed.
     */
    override suspend fun specFinished(
        ref: SpecRef,
        result: TestResult,
    ) {
        val name = checkNotNull(ref.kclass.qualifiedName)
        checkNotNull(this.results.firstOrNull { it.name == name }) { "specFinished event for class '$name' that hasn't been started." }
    }

    /**
     * Invoked if a [TestCase] is about to be executed.
     * Will not be invoked if the test is ignored.
     */
    override suspend fun testStarted(testCase: TestCase) {
        val name = checkNotNull(testCase.spec.javaClass.kotlin.qualifiedName)
        if (testCase.type != TestType.Container) {
            return
        }

        val current =
            checkNotNull(this.results.find { it.name == name }) {
                "testStarted event for class '$name' and test '${testCase.name.name}' that hasn't been started."
            }

        current.add(
            node = TestNode.Container(name = testCase.name.name),
            parentNames = testCase.parentsToList(),
        )
    }

    /**
     * Invoked if a [TestCase] will be skipped.
     */
    override suspend fun testIgnored(
        testCase: TestCase,
        reason: String?,
    ) {
        val name = checkNotNull(testCase.spec.javaClass.kotlin.qualifiedName)
        val current =
            checkNotNull(this.results.find { it.name == name }) {
                "testIgnored event for class '$name' and test '${testCase.name.name}' that hasn't been started."
            }

        current.add(
            node =
                if (testCase.type == TestType.Container) {
                    TestNode.Container(name = testCase.name.name)
                } else {
                    TestNode.Test(
                        name = testCase.name.name,
                        status = TestStatus.Ignored(reason = reason),
                        duration = Duration.ZERO,
                    )
                },
            parentNames = testCase.parentsToList(),
        )
    }

    /**
     * Invoked when all the invocations of a [TestCase] have completed.
     * This function will only be invoked if a test case was enabled.
     */
    override suspend fun testFinished(
        testCase: TestCase,
        result: TestResult,
    ) {
        val name = checkNotNull(testCase.spec.javaClass.kotlin.qualifiedName)
        if (testCase.type == TestType.Container) {
            return
        }

        val current =
            checkNotNull(this.results.find { it.name == name }) {
                "testFinished event for class '$name' and test '${testCase.name.name}' that hasn't been started."
            }

        current.add(
            node =
                TestNode.Test(
                    name = testCase.name.name,
                    status = TestStatus.from(result),
                    duration = result.duration,
                ),
            parentNames = testCase.parentsToList(),
        )
    }
}

internal fun TestCase.parentsToList(): List<String> {
    val testCase = this

    return buildList {
        var parent = testCase.parent
        while (parent != null) {
            this.add(parent.name.name)
            parent = parent.parent
        }
    }.reversed()
}
