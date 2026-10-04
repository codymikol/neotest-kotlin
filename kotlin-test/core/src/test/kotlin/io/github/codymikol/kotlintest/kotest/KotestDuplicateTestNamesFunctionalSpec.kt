package io.github.codymikol.kotlintest.kotest

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.execute.Status
import io.github.codymikol.kotlintest.execute.TestRunResult
import io.github.codymikol.kotlintest.execute.kotest.KotestTestExecutor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.File

private val CLASS_NAME = checkNotNull(KotestDuplicateExample::class.qualifiedName)

private suspend fun runDuplicateExample(filter: String? = null): Map<String, Status> {
    val result = KotestTestExecutor.run(classes = listOf(KotestDuplicateExample::class), filter = filter)
    val report = result.shouldBeInstanceOf<TestRunResult.Success>().report

    report.forEach { it.className shouldBe CLASS_NAME }
    report.map { it.id }.distinct().size shouldBe report.size

    return report.associate { it.id to it.status.type }
}

class KotestDuplicateTestNamesFunctionalSpec :
    FunSpec({
        context("duplicate test names") {
            test("discovered ids match executed ids") {
                val ktFile = createKtFile(
                    "KotestDuplicateExample.kt",
                    File("src/test/kotlin/io/github/codymikol/kotlintest/kotest/KotestDuplicateExample.kt").readText(),
                )

                val discovered = TestDiscoverer.discoverAllTests(setOf(ktFile))
                val discoveredTestIds = discovered.tests
                    .flatMap { it.allTests() }
                    .filterIsInstance<Discovered.Test>()
                    .map { it.id.removePrefix("$CLASS_NAME::") }

                discoveredTestIds shouldContainExactlyInAnyOrder runDuplicateExample().keys
                discovered.warnings.map { it.message } shouldContainExactlyInAnyOrder listOf(
                    "Multiple tests defined with name 'pass'",
                    "Multiple tests defined with name 'pass'",
                    "Multiple tests defined with name 'context'",
                    "Multiple tests defined with name 'dup'",
                )
            }

            test("run all") {
                runDuplicateExample() shouldBe mapOf(
                    "pass#1" to Status.SUCCESS,
                    "pass#2" to Status.FAILURE,
                    "pass#3" to Status.SUCCESS,
                    "unique" to Status.SUCCESS,
                    "context#1::dup#1" to Status.SUCCESS,
                    "context#1::dup#2" to Status.FAILURE,
                    "context#2::dup" to Status.FAILURE,
                )
            }

            test("run first top-level duplicate") {
                runDuplicateExample("$CLASS_NAME::pass#1") shouldBe mapOf(
                    "pass#1" to Status.SUCCESS,
                    "pass#2" to Status.IGNORED,
                    "pass#3" to Status.IGNORED,
                    "unique" to Status.IGNORED,
                )
            }

            test("run second top-level duplicate") {
                runDuplicateExample("$CLASS_NAME::pass#2") shouldBe mapOf(
                    "pass#1" to Status.IGNORED,
                    "pass#2" to Status.FAILURE,
                    "pass#3" to Status.IGNORED,
                    "unique" to Status.IGNORED,
                )
            }

            test("run duplicate container") {
                runDuplicateExample("$CLASS_NAME::context#2") shouldBe mapOf(
                    "pass#1" to Status.IGNORED,
                    "pass#2" to Status.IGNORED,
                    "pass#3" to Status.IGNORED,
                    "unique" to Status.IGNORED,
                    "context#2::dup" to Status.FAILURE,
                )
            }

            test("run nested duplicate") {
                runDuplicateExample("$CLASS_NAME::context#1::dup#2") shouldBe mapOf(
                    "pass#1" to Status.IGNORED,
                    "pass#2" to Status.IGNORED,
                    "pass#3" to Status.IGNORED,
                    "unique" to Status.IGNORED,
                    "context#1::dup#1" to Status.IGNORED,
                    "context#1::dup#2" to Status.FAILURE,
                )
            }
        }
    })
