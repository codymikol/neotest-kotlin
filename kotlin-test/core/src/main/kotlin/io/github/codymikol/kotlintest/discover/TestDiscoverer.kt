package io.github.codymikol.kotlintest.discover

import io.github.codymikol.kotlintest.discover.junit.JUnitTestDiscoverer
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
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
            val result =
                discoverers
                    .flatMap { discoverer ->
                        files.map { file -> discoverer.discoverTests(file) }
                    }.fold(DiscoveredResult()) { acc, result ->
                        acc.copy(
                            tests = acc.tests + result.tests,
                            warnings = acc.warnings + result.warnings,
                        )
                    }

            return DiscoveredResult(
                tests = result.tests,
                warnings = result.warnings + result.tests.flatMap { it.duplicateTestWarnings() },
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
