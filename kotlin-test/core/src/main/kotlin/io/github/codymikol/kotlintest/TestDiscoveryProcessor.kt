package io.github.codymikol.kotlintest

import com.intellij.psi.util.childrenOfType
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtSuperTypeList
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.lambdaExpressionRecursiveVisitor
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.resolve.calls.util.createLookupLocation

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

@Throws(IllegalArgumentException::class)
internal fun KtExpression.determinePosition() : Position {
    val location = requireNotNull(this.createLookupLocation()?.location) {
        "KtExpression without a location"
    }

    return Position(
        start = location.position.line,
        end = location.position.line + text.count { it == '\n' },
        filename = location.filePath
    )
}

internal object KotestTestDiscoverer : TestDiscoverer {
    private fun KtCallExpression.findTests(): Set<DiscoveredTest> {
        val type = when (this.calleeExpression?.firstChild?.text) {
            "test" -> TestType.TEST
            "context" -> TestType.CONTAINER
            else -> return emptySet()
        }

        val test = DiscoveredTest(
            id = this.valueArguments
                .firstOrNull()
                ?.firstChild
                ?.text
                ?.trim('"') ?: return emptySet(),
            type = type,
            position = this.determinePosition()
        )

        return if (test.type == TestType.CONTAINER) {
            setOf(test) + this.lambdaArguments
                .mapNotNull { it.getLambdaExpression() }
                .flatMap { it.findTests() }
                .map { discoveredTest -> discoveredTest.copy(id = "${test.id}::${discoveredTest.id}") }
        } else {
            setOf(test)
        }
    }

    private fun KtLambdaExpression.findTests(): Set<DiscoveredTest> =
        this.bodyExpression
            ?.children
            ?.filterIsInstance<KtCallExpression>()
            ?.flatMap { callExpression ->  callExpression.findTests() }
            ?.toSet()
            .orEmpty()

    override fun discoverTests(kotlinFile: KtFile): Set<DiscoveredTest> = analyze(kotlinFile) {
        kotlinFile
            .childrenOfType<KtClass>()
            .flatMap { kotlinClass -> kotlinClass.superTypeListEntries }
            .mapNotNull { entry -> entry.lastChild as? KtValueArgumentList }
            .flatMap { argumentList -> argumentList.arguments }
            .flatMap { argument -> argument.children.toList() }
            .filterIsInstance<KtLambdaExpression>()
            .flatMap { it.findTests() }
            .toSet()
    }
}

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