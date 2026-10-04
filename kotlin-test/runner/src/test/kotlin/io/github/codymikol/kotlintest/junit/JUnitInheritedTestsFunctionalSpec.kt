package io.github.codymikol.kotlintest.junit

import io.github.codymikol.kotlintest.execute.Status
import io.github.codymikol.kotlintest.execute.TestRunResult
import io.github.codymikol.kotlintest.execute.junit.JUnitTestExecutor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private val CLASS_NAME = checkNotNull(JUnitInheritedExample::class.qualifiedName)

private fun runInheritedExample(filter: String? = null): Map<String, Status> {
    val result = JUnitTestExecutor.run(classes = listOf(JUnitInheritedExample::class), filter = filter)
    val report = result.shouldBeInstanceOf<TestRunResult.Success>().report

    report.forEach { it.className shouldBe CLASS_NAME }
    report.map { it.id }.distinct().size shouldBe report.size

    return report.associate { it.id to it.status.type }
}

/**
 * The ids of a full run are the ids discovery produces for [JUnitInheritedExample], which is
 * asserted by `JUnitInheritedTestsDiscoverySpec` of the core module.
 */
class JUnitInheritedTestsFunctionalSpec :
    FunSpec({
        context("tests inherited from an abstract base class") {
            test("run class") {
                runInheritedExample() shouldBe mapOf(
                    "own" to Status.SUCCESS,
                    "inheritedPass" to Status.SUCCESS,
                    "inheritedFail" to Status.FAILURE,
                    "InheritedNested::nestedInherited" to Status.SUCCESS,
                )
            }

            test("run inherited test") {
                runInheritedExample("$CLASS_NAME::inheritedFail") shouldBe mapOf(
                    "inheritedFail" to Status.FAILURE,
                )
            }

            test("run inherited nested class") {
                runInheritedExample("$CLASS_NAME::InheritedNested") shouldBe mapOf(
                    "InheritedNested::nestedInherited" to Status.SUCCESS,
                )
            }

            test("run inherited nested test") {
                runInheritedExample("$CLASS_NAME::InheritedNested::nestedInherited") shouldBe mapOf(
                    "InheritedNested::nestedInherited" to Status.SUCCESS,
                )
            }
        }
    })
