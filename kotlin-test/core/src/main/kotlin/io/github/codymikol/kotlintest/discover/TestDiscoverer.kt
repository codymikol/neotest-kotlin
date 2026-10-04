package io.github.codymikol.kotlintest.discover

import io.github.codymikol.kotlintest.discover.junit.JUnitTestDiscoverer
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.Discovered.Container
import io.github.codymikol.kotlintest.discover.model.Discovered.Test
import io.github.codymikol.kotlintest.discover.model.DiscoveredResult
import io.github.codymikol.kotlintest.discover.model.TestWarning
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

/**
 * Suffix appended to sibling tests/containers that share the same name, `#1`, `#2`, ...
 *
 * Kotest runs duplicate names as `name`, `(1) name`, `(2) name`, ... see `toDiscoveredName` in the
 * runner (`io.github.codymikol.kotlintest.execute.kotest`) for the mapping back to these names.
 */
internal fun disambiguatedName(name: String, index: Int): String = "$name#$index"

/**
 * Returns the names of [this] in order where every name that occurs more than once is
 * suffixed with `#1`, `#2`, ... in the order in which they occur.
 */
internal fun <T> List<T>.disambiguatedNames(name: (T) -> String): List<String> {
    val nameCounts = this.groupingBy(name).eachCount()
    val nameIndexes = mutableMapOf<String, Int>()

    return this.map { element ->
        val elementName = name(element)

        if ((nameCounts[elementName] ?: 0) > 1) {
            disambiguatedName(elementName, checkNotNull(nameIndexes.merge(elementName, 1, Int::plus)))
        } else {
            elementName
        }
    }
}

/**
 * Warns about every sibling, after the first, that shares a name with a previous sibling.
 *
 * Must be called before [disambiguateDuplicateNames].
 */
internal fun duplicateNameWarnings(tests: Collection<Discovered>): List<TestWarning> =
    tests
        .groupBy { it.name }
        .values
        .filter { it.size > 1 }
        .flatMap { duplicates ->
            duplicates.drop(1).map { duplicate ->
                TestWarning(
                    message = "Multiple tests defined with name '${duplicate.name}'",
                    position = duplicate.position,
                )
            }
        } +
        tests.filterIsInstance<Container>().flatMap { duplicateNameWarnings(it.tests) }

/**
 * Appends `#1`, `#2`, ... to sibling tests/containers in this [Container] that share the same name,
 * so that every [Discovered.id] is unique. Siblings are numbered in their definition order which
 * matches the order in which Kotest registers (and renames) them.
 */
internal fun Container.disambiguateDuplicateNames(): Container =
    copy(tests = disambiguateDuplicateNames(id, tests))

internal fun disambiguateDuplicateNames(
    parentId: String,
    tests: Set<Discovered>,
): Set<Discovered> {
    val ordered = tests.toList()

    return ordered
        .zip(ordered.disambiguatedNames { it.name })
        .map { (test, disambiguatedName) ->
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
