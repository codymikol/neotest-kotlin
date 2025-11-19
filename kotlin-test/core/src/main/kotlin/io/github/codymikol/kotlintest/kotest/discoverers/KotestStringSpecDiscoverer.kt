package io.github.codymikol.kotlintest.kotest.discoverers

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.determinePosition
import io.kotest.core.spec.style.StringSpec
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import kotlin.collections.orEmpty

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#string-spec)
 */
internal object KotestStringSpecDiscoverer : KotestTestTypeDiscoverer {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == StringSpec::class.java.simpleName

    override fun discoverTests(lambda: KtLambdaExpression): Set<DiscoveredTest> =
        lambda.bodyExpression
            ?.children
            ?.filterIsInstance<KtCallExpression>()
            ?.map { callExpression ->
                DiscoveredTest(
                    id = callExpression.firstChild.text.trim('"'),
                    position = callExpression.determinePosition(),
                    type = TestType.TEST
                )
            }
            ?.toSet()
            .orEmpty()
}