package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.kotest.KotestTestDiscoverer
import org.jetbrains.kotlin.psi.KtFile

/**
 * Base interface for all test framework discoverers.
 */
public interface TestDiscoverer {
    public companion object {
        /**
         * All [TestDiscoverer]s that will be used in [discoverAllTests].
         */
        internal val discoverers: List<TestDiscoverer> = listOf(KotestTestDiscoverer)

        /**
         * Main entry point for test discovery. Executes all [discoverers] on the provided [KtFile]s.
         */
        public fun discoverAllTests(files: Set<KtFile>): Set<DiscoveredTest> =
            discoverers.flatMap { files.flatMap { file -> it.discoverTests(file) } }.toSet()
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
    public fun discoverTests(kotlinFile: KtFile): Set<DiscoveredTest>
}