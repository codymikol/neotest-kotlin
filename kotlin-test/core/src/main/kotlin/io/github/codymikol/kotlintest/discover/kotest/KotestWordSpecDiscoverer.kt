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
internal object KotestWordSpecDiscoverer : KotestExpressionTestTypeDiscoverer {
    private val CONTAINER_KEYWORDS = setOf("should", "Should", "When", "`when`")

    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == WordSpec::class.java.simpleName

    private fun KtExpression.findTests(parentId: String): Set<Discovered> =
        this
            .children
            .filterIsInstance<KtExpression>()
            .flatMap { expression ->
                when (expression) {
                    is KtBinaryExpression -> {
                        if (expression.operationReference.text !in CONTAINER_KEYWORDS) {
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
                                    ?.bodyExpression
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
            }.toSet()

    override fun discoverTests(
        expression: KtExpression?,
        classFqn: String,
    ): Set<Discovered> = expression?.findTests(classFqn).orEmpty()
}
