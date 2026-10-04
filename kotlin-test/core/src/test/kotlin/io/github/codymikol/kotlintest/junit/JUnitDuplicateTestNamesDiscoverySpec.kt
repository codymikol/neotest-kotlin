package io.github.codymikol.kotlintest.junit

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.runnerFixture
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

private const val CLASS_NAME = "io.github.codymikol.kotlintest.junit.JUnitDuplicateExample"

class JUnitDuplicateTestNamesDiscoverySpec :
    FunSpec({
        context("duplicate display names") {
            test("discovered ids match executed ids") {
                val ktFile = createKtFile(
                    "JUnitDuplicateExample.kt",
                    runnerFixture("junit/JUnitDuplicateExample.kt").readText(),
                )

                val discovered = TestDiscoverer.discoverAllTests(setOf(ktFile))
                val discoveredTests = discovered.tests
                    .flatMap { it.allTests() }
                    .filterIsInstance<Discovered.Test>()

                // numbered by method/class name, not by source order, matching the ids of running
                // all tests, see JUnitDuplicateTestNamesFunctionalSpec of the runner
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
        }
    })
