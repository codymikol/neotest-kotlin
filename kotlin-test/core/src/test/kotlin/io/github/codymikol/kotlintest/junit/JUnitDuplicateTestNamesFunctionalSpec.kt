package io.github.codymikol.kotlintest.junit

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.execute.Status
import io.github.codymikol.kotlintest.execute.TestRunResult
import io.github.codymikol.kotlintest.execute.junit.JUnitTestExecutor
import io.github.codymikol.kotlintest.execute.junit.toJUnitSelectors
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.platform.engine.discovery.DiscoverySelectors
import java.io.File

private val CLASS_NAME = checkNotNull(JUnitDuplicateExample::class.qualifiedName)

private val ALL_RESULTS = mapOf(
    "same#1" to Status.SUCCESS,
    "same#2" to Status.FAILURE,
    "unique" to Status.SUCCESS,
    "group#1::fail" to Status.FAILURE,
    "group#2::pass" to Status.SUCCESS,
)

private suspend fun runDuplicateExample(filter: String? = null): Map<String, Status> {
    val result = JUnitTestExecutor.run(classes = listOf(JUnitDuplicateExample::class), filter = filter)
    val report = result.shouldBeInstanceOf<TestRunResult.Success>().report

    report.forEach { it.className shouldBe CLASS_NAME }
    report.map { it.id }.distinct().size shouldBe report.size

    return report.associate { it.id to it.status.type }
}

class JUnitDuplicateTestNamesFunctionalSpec :
    FunSpec({
        context("duplicate display names") {
            test("discovered ids match executed ids") {
                val ktFile = createKtFile(
                    "JUnitDuplicateExample.kt",
                    File("src/test/kotlin/io/github/codymikol/kotlintest/junit/JUnitDuplicateExample.kt").readText(),
                )

                val discovered = TestDiscoverer.discoverAllTests(setOf(ktFile))
                val discoveredTests = discovered.tests
                    .flatMap { it.allTests() }
                    .filterIsInstance<Discovered.Test>()

                discoveredTests.map { it.id.removePrefix("$CLASS_NAME::") } shouldContainExactlyInAnyOrder
                    ALL_RESULTS.keys

                // numbered by method/class name, not by source order
                discoveredTests.associate { it.id.removePrefix("$CLASS_NAME::") to it.position.startLine } shouldBe
                    mapOf(
                        "same#2" to 17,
                        "same#1" to 23,
                        "unique" to 28,
                        "group#2::pass" to 36,
                        "group#1::fail" to 45,
                    )

                discovered.warnings.map { it.message } shouldContainExactlyInAnyOrder listOf(
                    "Multiple tests defined with name 'same'",
                    "Multiple tests defined with name 'group'",
                )
            }

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
