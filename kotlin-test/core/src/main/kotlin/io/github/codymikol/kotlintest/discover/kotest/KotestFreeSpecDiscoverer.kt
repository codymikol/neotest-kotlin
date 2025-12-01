package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.DiscoveredTest
import io.github.codymikol.kotlintest.discover.TestType
import io.github.codymikol.kotlintest.discover.determinePosition
import io.kotest.core.spec.style.FreeSpec
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import kotlin.collections.orEmpty

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#free-spec)
 */
internal object KotestFreeSpecDiscoverer : KotestTestTypeDiscoverer {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == FreeSpec::class.java.simpleName

    private fun KtLambdaExpression.findTests(): Set<DiscoveredTest> =
        this.bodyExpression
            ?.children
            ?.filterIsInstance<KtExpression>()
            ?.flatMap { expression ->
                when (expression) {
                    is KtBinaryExpression -> {
                        if (expression.operationReference.text != "-") {
                            return@flatMap emptyList()
                        }

                        val container = DiscoveredTest(
                            id = expression.firstChild.text.trim('"'),
                            position = expression.determinePosition(),
                            type = TestType.CONTAINER
                        )

                        listOf(container) + (expression.lastChild as? KtLambdaExpression)
                            ?.findTests()
                            .orEmpty()
                            .map { test ->
                                test.copy(id = "${container.id}::${test.id}")
                            }
                    }

                    is KtCallExpression -> {
                        listOf(
                            DiscoveredTest(
                                id = expression.firstChild.text.trim('"'),
                                position = expression.determinePosition(),
                                type = TestType.TEST
                            )
                        )
                    }

                    else -> emptyList()
                }
            }
            ?.toSet()
            .orEmpty()

    override fun discoverTests(lambda: KtLambdaExpression): Set<DiscoveredTest> =
        lambda.findTests()
}
