package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.determinePosition
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import kotlin.collections.orEmpty

internal sealed class KotestKtExpressionDiscoverer : KotestLambdaExpressionTestTypeDiscoverer {
    /**
     * keywords used to represent containers.
     */
    abstract val containers: List<String>

    /**
     * keywords used to represent leaf node tests.
     */
    abstract val tests: List<String>

    private fun KtCallExpression.findTests(parentId: String): Set<Discovered> {
        val id =
            this.valueArguments
                .firstOrNull()
                ?.firstChild
                ?.text
                ?.trim('"') ?: return emptySet()

        val fullId = "$parentId::$id"

        return when (this.calleeExpression?.firstChild?.text) {
            in tests ->
                setOf(
                    Discovered.Test(
                        id = fullId,
                        name = id,
                        position = this.determinePosition(),
                    ),
                )
            in containers ->
                setOf(
                    Discovered.Container(
                        id = fullId,
                        name = id,
                        position = this.determinePosition(),
                        tests =
                        this.lambdaArguments
                            .mapNotNull { it.getLambdaExpression() }
                            .flatMap { it.findTests(fullId) }
                            .toSet(),
                    ),
                )
            else -> emptySet()
        }
    }

    private fun KtLambdaExpression.findTests(parentId: String): Set<Discovered> =
        this.bodyExpression
            ?.children
            ?.filterIsInstance<KtCallExpression>()
            ?.flatMap { callExpression -> callExpression.findTests(parentId) }
            ?.toSet()
            .orEmpty()

    override fun discoverTests(
        lambda: KtLambdaExpression,
        classFqn: String,
    ): Set<Discovered> = lambda.findTests(classFqn)
}
