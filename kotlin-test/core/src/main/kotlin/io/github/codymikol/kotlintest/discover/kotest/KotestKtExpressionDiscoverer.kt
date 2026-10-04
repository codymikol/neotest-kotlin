package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.determinePosition
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression

internal sealed class KotestKtExpressionDiscoverer : KotestExpressionTestTypeDiscoverer {
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
                            .flatMap { it.bodyExpression?.findTests(fullId).orEmpty() }
                            .toSet(),
                    ),
                )
            else -> emptySet()
        }
    }

    private fun KtExpression.findTests(parentId: String): Set<Discovered> =
        this
            .children
            .filterIsInstance<KtCallExpression>()
            .flatMap { callExpression -> callExpression.findTests(parentId) }
            .toSet()

    override fun discoverTests(
        expression: KtExpression?,
        classFqn: String,
    ): Set<Discovered> = expression?.findTests(classFqn).orEmpty()
}
