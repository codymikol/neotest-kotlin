package io.github.codymikol.kotlintest.junit

import io.github.codymikol.kotlintest.byId
import io.github.codymikol.kotlintest.discoverWithClasspath
import io.github.codymikol.kotlintest.runnerFixture
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File

private const val CLASS_NAME = "io.github.codymikol.kotlintest.junit.JUnitInheritedExample"

private val base = runnerFixture("junit/JUnitInheritedBase.kt")
private val example = runnerFixture("junit/JUnitInheritedExample.kt")

/**
 * The ids discovered here are the ids of executing the same fixture, see `JUnitInheritedTestsFunctionalSpec`
 * of the runner.
 */
class JUnitInheritedTestsDiscoverySpec :
    FunSpec({
        context("tests inherited from an abstract base class") {
            test("discovered ids match executed ids") {
                val result = discoverWithClasspath(listOf(base, example), include = example)

                result.warnings shouldHaveSize 0
                result.tests.single().allTests().map { it.id }.toList() shouldContainExactlyInAnyOrder listOf(
                    "$CLASS_NAME::own",
                    "$CLASS_NAME::inheritedPass",
                    "$CLASS_NAME::inheritedFail",
                    "$CLASS_NAME::InheritedNested",
                    "$CLASS_NAME::InheritedNested::nestedInherited",
                )
            }

            test("positions of inherited tests point at the base class") {
                val discovered = discoverWithClasspath(listOf(base, example), include = example).byId()

                File(discovered.getValue("$CLASS_NAME::inheritedFail").position.filename).name shouldBe
                    "JUnitInheritedBase.kt"
                File(discovered.getValue("$CLASS_NAME::own").position.filename).name shouldBe
                    "JUnitInheritedExample.kt"
            }

            test("abstract base class is not discovered") {
                discoverWithClasspath(listOf(base, example), include = base).tests shouldHaveSize 0
            }
        }
    })
