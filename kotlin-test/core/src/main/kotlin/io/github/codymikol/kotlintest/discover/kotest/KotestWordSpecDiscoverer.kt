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
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#word-spec)
 */
internal object KotestWordSpecDiscoverer : KotestExpressionTestTypeDiscoverer {
    override val factoryBuilder: String = "wordSpec"

    private val CONTAINER_KEYWORDS = setOf("should", "Should", "When", "`when`")

    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.getAllSuperClasses().any { fqn ->
            fqn == FqName("io.kotest.core.spec.style.WordSpec")
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
                if (expression.operationReference.text !in CONTAINER_KEYWORDS) {
                    return emptyList()
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
    }
}
