package io.github.codymikol.kotlintest.execute.junit

import io.github.codymikol.kotlintest.execute.RunReport
import io.github.codymikol.kotlintest.execute.TestResult
import io.github.codymikol.kotlintest.execute.TestStatus
import io.github.codymikol.kotlintest.execute.disambiguatedName
import io.github.codymikol.kotlintest.execute.junit.JUnitTestReporter.Companion.ENGINE_REGEX
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

internal class JUnitTestReporter : TestExecutionListener {
    private val lock = Any()
    private val results: MutableSet<TestResult> = mutableSetOf()

    // JUnit requires that we manually keep track of our own start times
    private val testStartTimes: ConcurrentHashMap<UniqueId, Instant> = ConcurrentHashMap()
    private lateinit var testPlan: TestPlan

    companion object {
        /**
         * JUnit represents the root container as the [engine:junit] or [engine:kotest] which
         * is the owning container of any particular class.
         */
        val ENGINE_REGEX = "^\\[engine:[^]]+\\]$".toRegex()
    }

    internal fun report(): RunReport = synchronized(lock) { this.results.toSet() }

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        this.testPlan = testPlan
    }

    override fun executionFinished(
        testIdentifier: TestIdentifier,
        testExecutionResult: TestExecutionResult,
    ) {
        if (testIdentifier.isEngineContainer()) {
            return
        }

        if (testIdentifier.isContainerButNotTestFactory()) {
            containerFinished(testIdentifier, testExecutionResult)
            return
        }

        val (className, id) = testPlan.toClassNameAndId(testIdentifier)
        synchronized(lock) {
            if (testIdentifier.isTestFactory()) {
                val children = results.filter { it.id.startsWith(id) }

                results.add(
                    TestResult(
                        className = className,
                        id = id,
                        status = children.toTestFactoryStatus(),
                        duration = children.fold(Duration.ZERO) { totalDuration, childTest ->
                            totalDuration + childTest.duration.toJavaDuration()
                        }.toKotlinDuration()
                    )
                )
            } else {
                results.add(
                    TestResult(
                        className = className,
                        id = id,
                        status = testExecutionResult.toTestStatus(),
                        duration =
                        Duration
                            .between(
                                checkNotNull(testStartTimes[testIdentifier.uniqueIdObject]),
                                Instant.now(),
                            ).toKotlinDuration(),
                    ),
                )
            }
        }
    }

    /**
     * Containers (classes) are only reported when they did not succeed, e.g. an exception in `@BeforeAll`,
     * as the tests they contain are not reported in that case.
     */
    private fun containerFinished(
        testIdentifier: TestIdentifier,
        testExecutionResult: TestExecutionResult,
    ) {
        if (testExecutionResult.status == TestExecutionResult.Status.SUCCESSFUL) {
            return
        }

        val (className, id) = testPlan.toClassNameAndId(testIdentifier)
        val error = testExecutionResult.throwable.getOrNull()
        val status =
            if (testExecutionResult.status == TestExecutionResult.Status.FAILED && error != null) {
                TestStatus.Failure.fromClassError(className, error)
            } else {
                testExecutionResult.toTestStatus()
            }

        synchronized(lock) {
            results.add(
                TestResult(
                    className = className,
                    id = id,
                    status = status,
                    duration = Duration.ZERO.toKotlinDuration(),
                ),
            )
        }
    }

    override fun executionSkipped(
        testIdentifier: TestIdentifier,
        reason: String?,
    ) {
        if (testIdentifier.isEngineContainer()) {
            return
        }

        when {
            testIdentifier.isContainer -> {
                testPlan
                    .getDescendants(testIdentifier)
                    .filterNotNull()
                    .filter { test -> !test.isContainer }
                    .forEach { test ->
                        val (className, id) = testPlan.toClassNameAndId(test)
                        synchronized(lock) {
                            results.add(
                                TestResult(
                                    className = className,
                                    id = id,
                                    status = TestStatus.Ignored(reason = reason),
                                    duration = Duration.ZERO.toKotlinDuration(),
                                ),
                            )
                        }
                    }
            }

            else -> {
                val (className, id) = testPlan.toClassNameAndId(testIdentifier)
                synchronized(lock) {
                    results.add(
                        TestResult(
                            className = className,
                            id = id,
                            status = TestStatus.Ignored(reason = reason),
                            duration = Duration.ZERO.toKotlinDuration(),
                        ),
                    )
                }
            }
        }
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        if (testIdentifier.isEngineContainer()) {
            return
        }

        if (!testIdentifier.isContainerButNotTestFactory()) {
            testStartTimes[testIdentifier.uniqueIdObject] = Instant.now()
        }
    }
}

/**
 * The status of a test factory is the first failure of its dynamic tests, or ignored
 * when all of them were ignored (e.g. failed assumptions).
 */
private fun List<TestResult>.toTestFactoryStatus(): TestStatus =
    firstOrNull { it.status is TestStatus.Failure }?.status
        ?: firstOrNull()?.status?.takeIf { _ -> all { it.status is TestStatus.Ignored } }
        ?: TestStatus.Success

internal val TestIdentifier.name: String
    get() =
        if (displayName.endsWith("()")) {
            displayName.substringBeforeLast('(')
        } else {
            displayName
        }

internal fun TestExecutionResult.toTestStatus(): TestStatus =
    when (status) {
        TestExecutionResult.Status.SUCCESSFUL -> TestStatus.Success
        TestExecutionResult.Status.FAILED -> TestStatus.Failure.from(throwable.getOrNull())
        // JUnit reports failed assumptions (and other aborts) as ABORTED
        TestExecutionResult.Status.ABORTED ->
            TestStatus.Ignored(reason = throwable.getOrNull()?.let { it.message ?: it.toString() })
    }

/**
 * Test Factories are containers, but need to be treated as normal tests because
 * dynamic tests that are part of the factory can't be discovered while the overarching factory
 * can be. Discovering the containing factory is the only way to show test results.
 */
internal fun TestIdentifier.isContainerButNotTestFactory(): Boolean =
    this.isContainer && !this.uniqueId.contains("test-factory")

internal fun TestIdentifier.isTestFactory(): Boolean =
    this.isContainer && this.uniqueId.contains("test-factory") && !this.uniqueId.contains("dynamic-test")

/**
 * Identifies if this [TestIdentifier] is a Container and if it's an Engine.
 */
internal fun TestIdentifier.isEngineContainer(): Boolean = this.isContainer && this.uniqueId.matches(ENGINE_REGEX)

internal fun TestPlan.getParentTestIdentifier(testIdentifier: TestIdentifier): TestIdentifier? =
    testIdentifier.parentIdObject.getOrNull()?.let { this.getTestIdentifier(it) }

/**
 * The [TestIdentifier.name] of a test method or `@Nested` class, suffixed with `#1`, `#2`, ... when
 * sibling methods/classes share the same display name, matching the names produced by discovery.
 *
 * Siblings are only known when their parent class is part of the [TestPlan], which is why
 * [toJUnitSelectors] selects the parent class when a filter targets a disambiguated test.
 */
internal fun TestPlan.disambiguatedName(testIdentifier: TestIdentifier): String {
    val parent = getParentTestIdentifier(testIdentifier)
    val isClassMember = parent?.source?.getOrNull() is ClassSource

    val duplicates = if (isClassMember && JUnitSiblingKey.from(testIdentifier) != null) {
        getChildren(checkNotNull(parent))
            .filter { sibling -> sibling.name == testIdentifier.name }
            .mapNotNull { sibling -> JUnitSiblingKey.from(sibling)?.let { key -> sibling to key } }
    } else {
        emptyList()
    }

    if (duplicates.size < 2) {
        return testIdentifier.name
    }

    val index = duplicates
        .sortedBy { (_, key) -> key }
        .indexOfFirst { (sibling, _) -> sibling.uniqueIdObject == testIdentifier.uniqueIdObject }

    return disambiguatedName(testIdentifier.name, index + 1)
}

internal typealias ClassName = String
internal typealias Id = String

internal fun TestPlan.toClassNameAndId(testIdentifier: TestIdentifier): Pair<ClassName, Id> {
    val testPlan = this
    var test: TestIdentifier? = testIdentifier

    val testCases =
        buildList {
            while (test != null && !test.isEngineContainer()) {
                val className = test.source.getOrNull()?.let { (it as? ClassSource)?.className }

                // only use className for top-level containers (classes) and not nested classes
                val name =
                    if (className != null && testPlan.getParentTestIdentifier(test)?.isEngineContainer() == true) {
                        className
                    } else {
                        testPlan.disambiguatedName(test)
                    }

                this.add(name)
                test = testPlan.getParentTestIdentifier(test)
            }
        }.reversed()

    return testCases.first() to testCases.subList(1, testCases.size).joinToString(separator = "::")
}
