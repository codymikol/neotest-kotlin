package io.github.codymikol.kotlintest.discover

import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.calls.util.createLookupLocation

public sealed interface Discovered {
    /**
     * Unique identifier for the test `::` separation signifies nesting.
     */
    public val id: String

    /**
     * The last segment of the [id] where applicable for specific tests or a more readable
     * version of the name.
     */
    public val name: String

    /**
     * File location and position in that file for the [DiscoveredTest].
     */
    public val position: Position

    /**
     * The type of test used to serialization/deserialization purposes.
     */
    public val type: TestType

    public fun copyWithId(id: String): Discovered

    public data class Test(
        override val id: String,
        override val name: String,
        override val position: Position,
    ) : Discovered {
        override val type: TestType = TestType.TEST

        override fun copyWithId(id: String): Discovered = copy(id = id)
    }

    /**
     * A namespace/container that could be a class or
     * a nested test.
     */
    public data class Container(
        override val id: String,
        override val name: String,
        override val position: Position,
        /**
         * Nested [Discovered] under this [Container].
         */
        val tests: Set<Discovered>,
    ) : Discovered {
        override val type: TestType = TestType.CONTAINER

        override fun copyWithId(id: String): Discovered = copy(id = id)
    }
}

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
    val location =
        requireNotNull(this.createLookupLocation()?.location) {
            "KtExpression without a location"
        }

    return Position(
        start = location.position.line,
        end = location.position.line + text.count { it == '\n' },
        filename = location.filePath,
    )
}
