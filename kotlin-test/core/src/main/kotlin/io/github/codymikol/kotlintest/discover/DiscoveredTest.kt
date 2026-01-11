package io.github.codymikol.kotlintest.discover

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
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

/**
 * Similar to [KtExpression.determinePosition], but strips out annotations from [Position.startLine]
 * and [Position.startColumn].
 *
 * ```
 * @Test
 *    fun example() {
 *   assertEquals(1, 1)
 * }
 * ```
 *
 * In the above example, startLine = 2 and startColumn = 4
 */
internal fun KtNamedFunction.determinePosition(): Position {
    val expressionPosition = (this as KtExpression).determinePosition()

    return expressionPosition.copy(
        startLine = expressionPosition.startLine + text.substringBefore("fun").count { it == '\n' },
        startColumn = text
            .substringBefore("fun")
            .substringAfterLast("\n")
            .takeWhile { it.isWhitespace() }
            .length + 1
    )
}

/**
 * Similar to [KtExpression.determinePosition], but strips out annotations from [Position.startLine]
 * and [Position.startColumn].
 *
 * ```
 * @Nested
 *    inner class Example {
 *   @Test
 *   fun example() {
 *     assertEquals(1, 1)
 *   }
 * }
 * ```
 *
 * In the above example, startLine = 2 and startColumn = 4
 */
internal fun KtClass.determinePosition(): Position {
    val expressionPosition = (this as KtExpression).determinePosition()

    val name = checkNotNull(this.name) {
        @Suppress("MaxLineLength") // error message with context is long
        "class without a name at ${expressionPosition.filename} line ${expressionPosition.startLine} column ${expressionPosition.startColumn}"
    }

    return expressionPosition.copy(
        startLine = expressionPosition.startLine + text.substringBefore(name).count { it == '\n' },
        startColumn = text
            .substringBefore(name)
            .substringAfterLast("\n")
            .takeWhile { it.isWhitespace() }
            .length + 1
    )
}

@Throws(IllegalArgumentException::class)
internal fun KtExpression.determinePosition(): Position {
    val location =
        requireNotNull(this.createLookupLocation()?.location) {
            "KtExpression without a location"
        }

    return Position(
        startLine = location.position.line,
        startColumn = location.position.column,
        endLine = location.position.line + text.count { it == '\n' },
        endColumn =
        text
            .substringAfterLast('\n')
            .takeWhile { it.isWhitespace() }
            .length + 1,
        filename = location.filePath,
    )
}
