package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.determinePosition
import io.kotest.core.spec.style.StringSpec
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#string-spec)
 */
internal object KotestStringSpecDiscoverer : KotestExpressionTestTypeDiscoverer {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == StringSpec::class.java.simpleName

    override fun discoverTests(
        expression: KtExpression?,
        classFqn: String,
    ): Set<Discovered> =
        expression
            ?.children
            ?.filterIsInstance<KtCallExpression>()
            ?.map { callExpression ->
                val id = callExpression.firstChild.text.trim('"')

                Discovered.Test(
                    id = "$classFqn::$id",
                    name = id,
                    position = callExpression.determinePosition(),
                )
            }?.toSet().orEmpty()
}
