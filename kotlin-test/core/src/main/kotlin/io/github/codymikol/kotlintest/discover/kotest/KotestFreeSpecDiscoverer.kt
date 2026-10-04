package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.determinePosition
import org.jetbrains.kotlin.name.FqName
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
    override val factoryBuilder: String = "freeSpec"

    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.getAllSuperClasses().any { fqn ->
            fqn == FqName("io.kotest.core.spec.style.FreeSpec")
        }

    private fun KtExpression.findTests(parentId: String): Set<Discovered> =
        this
            .children
            .filterIsInstance<KtExpression>()
            .flatMap { expression -> discoverStatement(expression, parentId) }
            .toSet()

    override fun discoverStatement(
        statement: KtExpression,
        parentId: String,
    ): List<Discovered> {
        return when (val expression = statement) {
            is KtBinaryExpression -> {
                if (expression.operationReference.text != "-") {
                    return emptyList()
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
    }
}
