package io.github.codymikol.kotlintest.kotest

import io.github.codymikol.kotlintest.execute.Status
import io.github.codymikol.kotlintest.execute.TestRunResult
import io.github.codymikol.kotlintest.execute.kotest.KotestTestExecutor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.KClass

private val INHERITED_CLASS_NAME = checkNotNull(KotestInheritedExample::class.qualifiedName)
private val DUPLICATE_CLASS_NAME = checkNotNull(KotestFactoryDuplicateExample::class.qualifiedName)

private fun run(kclass: KClass<*>, filter: String? = null): Map<String, Status> {
    val result = KotestTestExecutor.run(classes = listOf(kclass), filter = filter)
    val report = result.shouldBeInstanceOf<TestRunResult.Success>().report

    report.forEach { it.className shouldBe kclass.qualifiedName }
    report.map { it.id }.distinct().size shouldBe report.size

    return report.associate { it.id to it.status.type }
}

/**
 * The ids of a full run are the ids discovery produces for [KotestInheritedExample] and
 * [KotestFactoryDuplicateExample], which is asserted by `KotestInheritedTestsDiscoverySpec` of the core module.
 */
class KotestInheritedTestsFunctionalSpec :
    FunSpec({
        context("tests inherited from a base spec and included from test factories") {
            test("run all") {
                run(KotestInheritedExample::class) shouldBe mapOf(
                    "inherited before body" to Status.SUCCESS,
                    "own" to Status.SUCCESS,
                    "factory test" to Status.SUCCESS,
                    "factory container::factory nested" to Status.FAILURE,
                    "prefixed factory property test" to Status.SUCCESS,
                    "inherited container::inherited nested" to Status.FAILURE,
                    "own init" to Status.SUCCESS,
                )
            }

            test("run inherited test") {
                run(KotestInheritedExample::class, "$INHERITED_CLASS_NAME::inherited before body") shouldBe mapOf(
                    "inherited before body" to Status.SUCCESS,
                    "own" to Status.IGNORED,
                    "factory test" to Status.IGNORED,
                    "prefixed factory property test" to Status.IGNORED,
                    "own init" to Status.IGNORED,
                )
            }

            test("run nested inherited test") {
                run(
                    KotestInheritedExample::class,
                    "$INHERITED_CLASS_NAME::inherited container::inherited nested",
                ) shouldBe mapOf(
                    "inherited before body" to Status.IGNORED,
                    "own" to Status.IGNORED,
                    "factory test" to Status.IGNORED,
                    "prefixed factory property test" to Status.IGNORED,
                    "inherited container::inherited nested" to Status.FAILURE,
                    "own init" to Status.IGNORED,
                )
            }

            test("run nested factory test") {
                run(
                    KotestInheritedExample::class,
                    "$INHERITED_CLASS_NAME::factory container::factory nested",
                ) shouldBe mapOf(
                    "inherited before body" to Status.IGNORED,
                    "own" to Status.IGNORED,
                    "factory test" to Status.IGNORED,
                    "factory container::factory nested" to Status.FAILURE,
                    "prefixed factory property test" to Status.IGNORED,
                    "own init" to Status.IGNORED,
                )
            }

            test("run prefixed factory test") {
                run(
                    KotestInheritedExample::class,
                    "$INHERITED_CLASS_NAME::prefixed factory property test",
                ) shouldBe mapOf(
                    "inherited before body" to Status.IGNORED,
                    "own" to Status.IGNORED,
                    "factory test" to Status.IGNORED,
                    "prefixed factory property test" to Status.SUCCESS,
                    "own init" to Status.IGNORED,
                )
            }
        }

        context("test factory included twice") {
            test("run all") {
                run(KotestFactoryDuplicateExample::class) shouldBe mapOf(
                    "factory test#1" to Status.SUCCESS,
                    "factory test#2" to Status.SUCCESS,
                    "factory container#1::factory nested" to Status.FAILURE,
                    "factory container#2::factory nested" to Status.FAILURE,
                )
            }

            test("run second inclusion") {
                run(KotestFactoryDuplicateExample::class, "$DUPLICATE_CLASS_NAME::factory container#2") shouldBe mapOf(
                    "factory test#1" to Status.IGNORED,
                    "factory test#2" to Status.IGNORED,
                    "factory container#2::factory nested" to Status.FAILURE,
                )
            }
        }
    })
