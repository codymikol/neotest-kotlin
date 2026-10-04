package io.github.codymikol.kotlintest.kotest

import io.github.codymikol.kotlintest.byId
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discoverWithClasspath
import io.github.codymikol.kotlintest.idsInOrder
import io.github.codymikol.kotlintest.runnerFixture
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe

private const val PACKAGE = "io.github.codymikol.kotlintest.kotest"
private const val INHERITED = "$PACKAGE.KotestInheritedExample"
private const val DUPLICATE = "$PACKAGE.KotestFactoryDuplicateExample"

private val base = runnerFixture("kotest/KotestInheritedBase.kt")
private val factories = runnerFixture("kotest/KotestFactories.kt")
private val inherited = runnerFixture("kotest/KotestInheritedExample.kt")
private val duplicate = runnerFixture("kotest/KotestFactoryDuplicateExample.kt")
private val fixtures = listOf(base, factories, inherited, duplicate)

private fun Map<String, Discovered>.line(id: String): Pair<String, Int> {
    this shouldContainKey id
    val position = getValue(id).position
    return java.io.File(position.filename).name to position.startLine
}

/**
 * The ids discovered here are the ids of executing the same fixtures, see `KotestInheritedTestsFunctionalSpec`
 * of the runner.
 */
class KotestInheritedTestsDiscoverySpec :
    FunSpec({
        context("tests inherited from a base spec and included from test factories") {
            test("discovered ids match executed ids, in registration order") {
                val result = discoverWithClasspath(fixtures, include = inherited)

                result.warnings shouldHaveSize 0
                result.tests.single().idsInOrder() shouldContainExactly listOf(
                    INHERITED,
                    "$INHERITED::inherited before body",
                    "$INHERITED::own",
                    "$INHERITED::factory test",
                    "$INHERITED::factory container",
                    "$INHERITED::factory container::factory nested",
                    "$INHERITED::prefixed factory property test",
                    "$INHERITED::inherited container",
                    "$INHERITED::inherited container::inherited nested",
                    "$INHERITED::own init",
                )
            }

            test("positions point at the declarations") {
                val discovered = discoverWithClasspath(fixtures, include = inherited).byId()

                discovered.line("$INHERITED::inherited before body") shouldBe ("KotestInheritedBase.kt" to 11)
                discovered.line("$INHERITED::own") shouldBe ("KotestInheritedExample.kt" to 11)
                discovered.line("$INHERITED::factory test") shouldBe ("KotestFactories.kt" to 10)
                discovered.line("$INHERITED::factory container::factory nested") shouldBe ("KotestFactories.kt" to 15)
                discovered.line("$INHERITED::prefixed factory property test") shouldBe ("KotestFactories.kt" to 22)
                discovered.line("$INHERITED::inherited container::inherited nested") shouldBe
                    ("KotestInheritedBase.kt" to 18)
                discovered.line("$INHERITED::own init") shouldBe ("KotestInheritedExample.kt" to 19)
            }

            test("abstract base spec and factories have no tests of their own") {
                discoverWithClasspath(fixtures, include = base).tests shouldHaveSize 0
                discoverWithClasspath(fixtures, include = factories).tests shouldHaveSize 0
            }
        }

        context("test factory included twice") {
            test("discovered ids match executed ids") {
                val result = discoverWithClasspath(fixtures, include = duplicate)

                result.tests.single().idsInOrder() shouldContainExactly listOf(
                    DUPLICATE,
                    "$DUPLICATE::factory test#1",
                    "$DUPLICATE::factory container#1",
                    "$DUPLICATE::factory container#1::factory nested",
                    "$DUPLICATE::factory test#2",
                    "$DUPLICATE::factory container#2",
                    "$DUPLICATE::factory container#2::factory nested",
                )
                result.warnings.map { it.message } shouldContainExactlyInAnyOrder listOf(
                    "Multiple tests defined with name 'factory test'",
                    "Multiple tests defined with name 'factory container'",
                )
            }
        }
    })
