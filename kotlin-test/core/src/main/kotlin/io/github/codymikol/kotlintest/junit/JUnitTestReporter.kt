package io.github.codymikol.kotlintest.junit

import io.github.codymikol.kotlintest.RunReport
import io.github.codymikol.kotlintest.TestNode
import io.github.codymikol.kotlintest.TestStatus
import io.github.codymikol.kotlintest.junit.JUnitTestReporter.Companion.ENGINE_REGEX
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
import kotlin.time.toKotlinDuration
import kotlin.time.Duration as KotlinDuration

internal class JUnitTestReporter : TestExecutionListener {
    private val results: MutableSet<TestNode.Container> = mutableSetOf()

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

    internal fun report(): RunReport = this.results.toSet()

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        this.testPlan = testPlan
    }

    override fun dynamicTestRegistered(testIdentifier: TestIdentifier) {
        if (!testIdentifier.isContainer || testIdentifier.uniqueId.matches(ENGINE_REGEX)) {
            return
        }

        val name = testIdentifier.source.getOrNull()?.let { (it as? ClassSource)?.className } ?: testIdentifier.displayName
        val parents = testPlan.parentsToList(testIdentifier)

        if (parents.isEmpty()) {
            results.add(TestNode.Container(checkNotNull(name)))
        } else {
            val topLevelName = checkNotNull(parents.firstOrNull())
            val topLevelContainer = checkNotNull(results.find { it.name == topLevelName })
            topLevelContainer.add(TestNode.Container(name), parents.subList(1, parents.size))
        }
    }

    override fun executionFinished(
        testIdentifier: TestIdentifier,
        testExecutionResult: TestExecutionResult,
    ) {
        if (testIdentifier.isContainer || testIdentifier.uniqueId.matches(ENGINE_REGEX)) {
            return
        }

        val parents = testPlan.parentsToList(testIdentifier)
        val topLevelName = parents.firstOrNull() ?: return
        val topLevelContainer = results.find { it.name == topLevelName } ?: return

        topLevelContainer.add(
            TestNode.Test(
                name = testIdentifier.displayName,
                duration =
                    Duration
                        .between(
                            checkNotNull(testStartTimes[testIdentifier.uniqueIdObject]),
                            Instant.now(),
                        ).toKotlinDuration(),
                status = TestStatus.from(testExecutionResult),
            ),
            parents.subList(1, parents.size),
        )
    }

    override fun executionSkipped(
        testIdentifier: TestIdentifier,
        reason: String?,
    ) {
        if (testIdentifier.uniqueId.matches(ENGINE_REGEX)) {
            return
        }

        when {
            testIdentifier.isContainer -> {
                val name = testIdentifier.source.getOrNull()?.let { (it as? ClassSource)?.className } ?: testIdentifier.displayName
                val parents = testPlan.parentsToList(testIdentifier)

                // top level container
                val topLevelContainer =
                    if (parents.isEmpty()) {
                        TestNode.Container(checkNotNull(name)).also {
                            results.add(it)
                        }
                    } else {
                        val topLevelName = checkNotNull(parents.firstOrNull())
                        checkNotNull(results.find { it.name == topLevelName })
                    }

                testPlan
                    .getDescendants(testIdentifier)
                    .filterNotNull()
                    .forEach { test ->
                        val testNode =
                            if (test.isContainer) {
                                TestNode.Container(testIdentifier.displayName)
                            } else {
                                TestNode.Test(
                                    name = testIdentifier.displayName,
                                    duration = KotlinDuration.ZERO,
                                    status =
                                        TestStatus.Ignored(reason = reason),
                                )
                            }

                        topLevelContainer.add(testNode, parents.subList(1, parents.size))
                    }
            }

            else -> {
                val parents = testPlan.parentsToList(testIdentifier)
                val topLevelName = parents.firstOrNull() ?: return
                val topLevelContainer = results.find { it.name == topLevelName } ?: return

                topLevelContainer.add(
                    TestNode.Test(
                        name = testIdentifier.displayName,
                        duration = KotlinDuration.ZERO,
                        status =
                            TestStatus.Ignored(reason = reason),
                    ),
                    parents.subList(1, parents.size),
                )
            }
        }
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        val source = testIdentifier.source.getOrNull()
        if (!testIdentifier.isContainer || source == null || testIdentifier.uniqueId.matches(ENGINE_REGEX)) {
            testStartTimes[testIdentifier.uniqueIdObject] = Instant.now()
            return
        }

        when {
            // Class container
            source is ClassSource -> {
                val parents = testPlan.parentsToList(testIdentifier)
                val topLevelName = parents.firstOrNull()
                if (topLevelName == null) {
                    results.add(TestNode.Container(checkNotNull(source.className)))
                } else {
                    val topLevelContainer = results.find { it.name == topLevelName } ?: return
                    topLevelContainer.add(TestNode.Container(checkNotNull(source.className)), parents.subList(1, parents.size))
                }
            }

            // dynamic container
            else -> {
                val parents = testPlan.parentsToList(testIdentifier)
                val topLevelName = parents.firstOrNull() ?: return
                val topLevelContainer = results.find { it.name == topLevelName } ?: return
                topLevelContainer.add(TestNode.Container(testIdentifier.displayName), parents.subList(1, parents.size))
            }
        }
    }
}

internal fun TestPlan.getParentTestIdentifier(testIdentifier: TestIdentifier): TestIdentifier? =
    testIdentifier.parentIdObject.getOrNull()?.let { this.getTestIdentifier(it) }

internal fun TestPlan.parentsToList(testIdentifier: TestIdentifier): List<String> {
    val testPlan = this

    return buildList {
        var parentTestIdentifier = testPlan.getParentTestIdentifier(testIdentifier)
        while (parentTestIdentifier != null && !parentTestIdentifier.uniqueId.matches(ENGINE_REGEX)) {
            this.add(parentTestIdentifier.source.getOrNull()?.let { (it as? ClassSource)?.className } ?: parentTestIdentifier.displayName)
            parentTestIdentifier = testPlan.getParentTestIdentifier(parentTestIdentifier)
        }
    }.reversed()
}
