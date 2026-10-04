package io.github.codymikol.kotlintest.junit

import io.github.codymikol.kotlintest.execute.Status
import io.github.codymikol.kotlintest.execute.TestRunResult
import io.github.codymikol.kotlintest.execute.junit.JUnitTestExecutor
import io.github.codymikol.kotlintest.execute.junit.toJUnitSelectors
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.platform.engine.discovery.DiscoverySelectors

private val CLASS_NAME = checkNotNull(JUnitDuplicateExample::class.qualifiedName)

/**
 * These are the ids discovery produces for [JUnitDuplicateExample], which is asserted by
 * `JUnitDuplicateTestNamesDiscoverySpec` of the core module.
 */
private val ALL_RESULTS = mapOf(
    "same#1" to Status.SUCCESS,
    "same#2" to Status.FAILURE,
    "unique" to Status.SUCCESS,
    "group#1::fail" to Status.FAILURE,
    "group#2::pass" to Status.SUCCESS,
)

private fun runDuplicateExample(filter: String? = null): Map<String, Status> {
    val result = JUnitTestExecutor.run(classes = listOf(JUnitDuplicateExample::class), filter = filter)
    val report = result.shouldBeInstanceOf<TestRunResult.Success>().report

    report.forEach { it.className shouldBe CLASS_NAME }
    report.map { it.id }.distinct().size shouldBe report.size

    return report.associate { it.id to it.status.type }
}

class JUnitDuplicateTestNamesFunctionalSpec :
    FunSpec({
        context("duplicate display names") {
            withData(
                nameFn = { filter -> "run ${filter ?: "class"}" },
                null,
                "$CLASS_NAME::same#1",
                "$CLASS_NAME::same#2",
                "$CLASS_NAME::group#1",
                "$CLASS_NAME::group#2::pass",
            ) { filter ->
                // JUnit can't select duplicates by display name, so the whole class is run
                runDuplicateExample(filter) shouldBe ALL_RESULTS
            }

            test("filter on a duplicate selects its parent") {
                listOf(JUnitDuplicateExample::class).toJUnitSelectors("$CLASS_NAME::same#2") shouldBe
                    listOf(DiscoverySelectors.selectClass(JUnitDuplicateExample::class.java))
            }
        }
    })
