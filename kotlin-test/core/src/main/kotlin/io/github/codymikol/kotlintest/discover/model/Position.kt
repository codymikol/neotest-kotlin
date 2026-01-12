package io.github.codymikol.kotlintest.discover.model

public data class Position(
    /**
     * The full path to the file this [Position] references.
     */
    val filename: String,
    /**
     * Starting line number (starting with 1)
     */
    val startLine: Int,
    /**
     * Starting column number (starting with 1) on the [startLine].
     */
    val startColumn: Int,
    /**
     * Ending line number (starting with 1).
     */
    val endLine: Int,
    /**
     * Ending column number (starting with 1) on the [endLine].
     */
    val endColumn: Int,
)
