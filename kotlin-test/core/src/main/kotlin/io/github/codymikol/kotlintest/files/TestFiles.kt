package io.github.codymikol.kotlintest.files

import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.files.model.TestFilesResult
import org.jetbrains.kotlin.psi.KtFile

/**
 * Whether discovery finds at least one test class or spec in this [KtFile].
 *
 * Test files are determined with the same [TestDiscoverer]s used by discovery, so a file is a
 * test file exactly when discovering it yields tests: Kotest specs (including objects and
 * custom super classes, excluding abstract classes), JUnit and kotlin.test classes. Helper
 * files, test fixtures or a Kotest `ProjectConfig` are not test files.
 */
internal fun KtFile.isTestFile(): Boolean =
    TestDiscoverer.discoverers.any { discoverer -> discoverer.discoverTests(this).tests.isNotEmpty() }

/**
 * Determines which of these files are test files, see [isTestFile].
 */
internal fun Collection<KtFile>.findTestFiles(): TestFilesResult =
    TestFilesResult(
        testFiles = this
            .filter { it.isTestFile() }
            .map { it.virtualFilePath }
            .sorted(),
    )
