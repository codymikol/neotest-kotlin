package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.determinePosition
import io.kotest.core.spec.style.WordSpec
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import kotlin.collections.orEmpty

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#word-spec)
 */
internal object KotestWordSpecDiscoverer : KotestLambdaExpressionTestTypeDiscoverer {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == WordSpec::class.java.simpleName

    private fun KtLambdaExpression.findTests(parentId: String): Set<Discovered> =
        this.bodyExpression
            ?.children
            ?.filterIsInstance<KtExpression>()
            ?.flatMap { expression ->
                when (expression) {
                    is KtBinaryExpression -> {
                        if (expression.operationReference.text != "should") {
                            return@flatMap emptyList()
                        }

                        val id = expression.firstChild.text.trim('"')
                        val fullId = "$parentId::$id"

                        listOf(
                            Discovered.Container(
                                id = fullId,
                                position = expression.determinePosition(),
                                name = id,
                                tests =
                                (expression.lastChild as? KtLambdaExpression)
                                    ?.findTests(fullId)
                                    ?.toSet()
                                    .orEmpty(),
                            ),
                        )
                    }

                    is KtCallExpression -> {
                        val id = expression.firstChild.text.trim('"')

                        listOf(
                            Discovered.Test(
                                id = "$parentId::$id",
                                name = id,
                                position = expression.determinePosition(),
                            ),
                        )
                    }

                    else -> emptyList()
                }
            }?.toSet()
            .orEmpty()

    override fun discoverTests(
        lambda: KtLambdaExpression,
        classFqn: String,
    ): Set<Discovered> = lambda.findTests(classFqn)
}
