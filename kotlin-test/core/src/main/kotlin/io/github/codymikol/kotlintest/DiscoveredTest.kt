package io.github.codymikol.kotlintest

import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.calls.util.createLookupLocation

public data class DiscoveredTest(
    /**
     * Unique identifier for the test `::` separation signifies nesting.
     */
    val id: String,
    /**
     * File location and position in that file for the [DiscoveredTest].
     */
    val position: Position,
    val type: TestType,
)

public enum class TestType {
    TEST,
    CONTAINER,
}

public data class Position(
    /**
     * The full path to the file this [Position] references.
     */
    val filename: String,
    /**
     * Starting line number (starting with 1)
     */
    val start: Int,
    /**
     * Ending line number (starting with 1).
     */
    val end: Int,
)

@Throws(IllegalArgumentException::class)
internal fun KtExpression.determinePosition(): Position {
    val location = requireNotNull(this.createLookupLocation()?.location) {
        "KtExpression without a location"
    }

    return Position(
        start = location.position.line,
        end = location.position.line + text.count { it == '\n' },
        filename = location.filePath
    )
}
