package io.github.codymikol.neotestkotlin.framework.kotest

import io.github.codymikol.neotestkotlin.framework.RunReport
import io.github.codymikol.neotestkotlin.framework.TestNode
import io.github.codymikol.neotestkotlin.framework.TestStatus
import io.kotest.common.KotestInternal
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.core.test.TestType
import io.kotest.engine.interceptors.EngineContext
import io.kotest.engine.listener.TestEngineListener
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Implements Kotest's [TestEngineListener] for the sole purpose of observing spec/test completion
 * to create a [report].
 *
 * This reporter is **not** thread safe.
 */
@OptIn(KotestInternal::class)
class KotestTestReporter : TestEngineListener {
    private val results: MutableSet<TestNode.Container> = mutableSetOf()

    fun report(): RunReport = this.results.toSet()

    /**
     * Invoked as soon as the engine has been created.
     */
    override suspend fun engineStarted() {}

    /**
     * Invoked when the [TestEngine] has completed setup and is ready to begin
     * executing specs.
     *
     * @param context the final context that will be used.
     */
    override suspend fun engineInitialized(context: EngineContext) {}

    /**
     * Is invoked when the [TestEngine] has finished execution of all tests.
     *
     * If any unexpected errors were detected during execution then they will be
     * passed to this method.
     */
    override suspend fun engineFinished(t: List<Throwable>) {}

    /**
     * Invoked once per [Spec] to indicate that this spec will be instantiated
     * and any active tests invoked.
     */
    override suspend fun specStarted(kclass: KClass<*>) {
        val name = checkNotNull(kclass.qualifiedName)
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
     * Is invoked once per [Spec] class to indicate this spec has completed.
     */
    override suspend fun specFinished(
        kclass: KClass<*>,
        result: TestResult,
    ) {
        val name = checkNotNull(kclass.qualifiedName)
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
                "testStarted event for class '$name' and test '${testCase.name.testName}' that hasn't been started."
            }

        current.add(
            node = TestNode.Container(name = testCase.name.testName),
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
                "testIgnored event for class '$name' and test '${testCase.name.testName}' that hasn't been started."
            }

        current.add(
            node =
                if (testCase.type == TestType.Container) {
                    TestNode.Container(name = testCase.name.testName)
                } else {
                    TestNode.Test(
                        name = testCase.name.testName,
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
                "testFinished event for class '$name' and test '${testCase.name.testName}' that hasn't been started."
            }

        current.add(
            node =
                TestNode.Test(
                    name = testCase.name.testName,
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
            this.add(parent.name.testName)
            parent = parent.parent
        }
    }.reversed()
}
