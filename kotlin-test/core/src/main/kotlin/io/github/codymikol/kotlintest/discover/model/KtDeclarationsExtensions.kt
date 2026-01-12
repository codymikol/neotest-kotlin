package io.github.codymikol.kotlintest.discover.model

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.calls.util.createLookupLocation

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
