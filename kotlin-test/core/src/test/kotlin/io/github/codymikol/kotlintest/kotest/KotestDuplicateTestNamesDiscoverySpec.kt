package io.github.codymikol.kotlintest.kotest

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.runnerFixture
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

private const val CLASS_NAME = "io.github.codymikol.kotlintest.kotest.KotestDuplicateExample"

class KotestDuplicateTestNamesDiscoverySpec :
    FunSpec({
        context("duplicate test names") {
            test("discovered ids match executed ids") {
                val ktFile = createKtFile(
                    "KotestDuplicateExample.kt",
                    runnerFixture("kotest/KotestDuplicateExample.kt").readText(),
                )

                val discovered = TestDiscoverer.discoverAllTests(setOf(ktFile))
                val discoveredTestIds = discovered.tests
                    .flatMap { it.allTests() }
                    .filterIsInstance<Discovered.Test>()
                    .map { it.id.removePrefix("$CLASS_NAME::") }

                // the ids of running all tests, see KotestDuplicateTestNamesFunctionalSpec of the runner
                discoveredTestIds shouldContainExactlyInAnyOrder listOf(
                    "pass#1",
                    "pass#2",
                    "pass#3",
                    "unique",
                    "context#1::dup#1",
                    "context#1::dup#2",
                    "context#2::dup",
                )
                discovered.warnings.map { it.message } shouldContainExactlyInAnyOrder listOf(
                    "Multiple tests defined with name 'pass'",
                    "Multiple tests defined with name 'pass'",
                    "Multiple tests defined with name 'context'",
                    "Multiple tests defined with name 'dup'",
                )
            }
        }
    })
