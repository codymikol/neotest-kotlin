package io.github.codymikol.kotlintest

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtSuperTypeList
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.KtVisitorVoid
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
    override fun discoverTests(kotlinFile: KtFile): Set<DiscoveredTest> = analyze(kotlinFile) {
        val results = mutableSetOf<DiscoveredTest>()

        kotlinFile.acceptChildren(object : KtVisitorVoid() {
            override fun visitClass(klass: KtClass) {
                klass.acceptChildren(object : KtVisitorVoid() {
                    override fun visitSuperTypeList(list: KtSuperTypeList) {
                        list.entries.forEach { entry ->
                            entry.acceptChildren(object : KtVisitorVoid() {
                                override fun visitValueArgumentList(list: KtValueArgumentList) {
                                    list.arguments.map { argument ->
                                        argument.acceptChildren(object : KtVisitorVoid() {
                                            override fun visitLambdaExpression(lambdaExpression: KtLambdaExpression) {
                                                lambdaExpression.bodyExpression?.acceptChildren(object : KtVisitorVoid() {
                                                    override fun visitCallExpression(expression: KtCallExpression) {
                                                        val type = when (expression.calleeExpression?.firstChild?.text) {
                                                            "test" -> TestType.TEST
                                                            "context" -> TestType.CONTAINER
                                                            else -> return
                                                        }

                                                        results.add(DiscoveredTest(
                                                            id = expression.valueArguments.first().firstChild.text.trim('"'),
                                                            type = type,
                                                            position = expression.determinePosition()
                                                        ))
                                                    }
                                                })
                                            }
                                        })
                                    }
                                }
                            })
                        }
                    }
                })
            }
        })

        return results
    }
}

public data class DiscoveredTest(
    val id: String,
    /**
     * Line number position in the file.
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