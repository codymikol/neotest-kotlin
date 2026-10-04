package io.github.codymikol.kotlintest.files.model

/**
 * Result of test file detection: the absolute paths of every file containing tests.
 */
public data class TestFilesResult(
    /**
     * Sorted absolute paths of the files in which discovery finds at least one test class/spec.
     */
    val testFiles: List<String>,
)
