package io.github.codymikol.kotlintest.discover

import io.github.codymikol.kotlintest.discover.junit.JUnitTestDiscoverer
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.Discovered.Container
import io.github.codymikol.kotlintest.discover.model.Discovered.Test
import io.github.codymikol.kotlintest.discover.model.DiscoveredResult
import org.jetbrains.kotlin.psi.KtFile

/**
 * Base interface for all test framework discoverers.
 */
public interface TestDiscoverer {
    public companion object {
        /**
         * All [TestDiscoverer]s that will be used in [discoverAllTests].
         */
        internal val discoverers: List<TestDiscoverer> =
            listOf(
                KotestTestDiscoverer,
                JUnitTestDiscoverer,
            )

        /**
         * Main entry point for test discovery. Executes all [discoverers] on the provided [KtFile]s.
         */
        public fun discoverAllTests(files: Set<KtFile>): DiscoveredResult {
            val results =
                discoverers
                    .flatMap { discoverer ->
                        files.map { file -> discoverer.discoverTests(file) }
                    }

            val joinedTests = results
                .flatMap { it.tests }
                .groupBy { it.id }
                .map { (_, tests) ->
                    tests.fold(tests.first()) { acc, test ->
                        acc.copy(tests = acc.tests + test.tests)
                    }
                }
                .toSet()

            return DiscoveredResult(
                tests = joinedTests,
                warnings = results.flatMap { it.warnings } + joinedTests.flatMap { it.duplicateTestWarnings() },
            )
        }
    }

    /**
     * Discover all tests for a [kotlinFile], returning [emptyList] if
     * this [kotlinFile] doesn't have any tests supported or discovered.
     *
     * Should be defined as
     *
     * ```
     * override fun discoverTests(kotlinFile: KtFile): Set<DiscoveredTest> = analyze(kotlinFile) {
     *   TODO()
     * }
     * ```
     */
    public fun discoverTests(kotlinFile: KtFile): DiscoveredResult
}

internal fun Container.disambiguateDuplicateNames():
    Container =
    copy(tests = disambiguateDuplicateNames(id, tests))

internal fun disambiguateDuplicateNames(
    parentId: String,
    tests: Set<Discovered>,
): Set<Discovered> {
    if (tests.isEmpty()) {
        return emptySet()
    }

    val nameCounts = tests.groupingBy { it.name }.eachCount()
    val nameIndexes = mutableMapOf<String, Int>()

    return tests.map { test ->

            val hasDuplicates = (nameCounts[test.name] ?: -1) > 1

            val suffix = when(hasDuplicates) {
                true ->  "#${nameIndexes.merge(test.name, 1, Int::plus)}"
                false -> ""
            }

            val disambiguatedName = "${test.name}$suffix"
            val disambiguatedId = "$parentId::$disambiguatedName"

            when (test) {
                is Test -> test.copy(id = disambiguatedId, name = disambiguatedName)
                is Container -> test.copy(
                    id = disambiguatedId,
                    name = disambiguatedName,
                    tests = disambiguateDuplicateNames(disambiguatedId, test.tests),
                )
            }

        }
        .toSet()
}
