package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.determinePosition
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
internal object KotestFreeSpecDiscoverer : KotestExpressionTestTypeDiscoverer {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == FreeSpec::class.java.simpleName

    private fun KtExpression.findTests(parentId: String): Set<Discovered> =
        this
            .children
            .filterIsInstance<KtExpression>()
            .flatMap { expression ->
                when (expression) {
                    is KtBinaryExpression -> {
                        if (expression.operationReference.text != "-") {
                            return@flatMap emptyList()
                        }

                        val id = expression.firstChild.text.trim('"')
                        val fullId = "$parentId::$id"

                        listOf(
                            Discovered.Container(
                                id = fullId,
                                name = id,
                                position = expression.determinePosition(),
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
