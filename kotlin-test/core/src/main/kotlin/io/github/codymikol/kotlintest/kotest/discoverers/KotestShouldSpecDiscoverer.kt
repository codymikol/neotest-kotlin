package io.github.codymikol.kotlintest.kotest.discoverers

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.determinePosition
import io.kotest.core.spec.style.ShouldSpec
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import kotlin.collections.orEmpty

internal object KotestShouldSpecDiscoverer : KotestTestTypeDiscoverer {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == ShouldSpec::class.java.simpleName

    private fun KtCallExpression.findTests(): Set<DiscoveredTest> {
        val type = when (this.calleeExpression?.firstChild?.text) {
            "should" -> TestType.TEST
            "context" -> TestType.CONTAINER
            else -> return emptySet()
        }

        val test = DiscoveredTest(
            id = this.valueArguments
                .firstOrNull()
                ?.firstChild
                ?.text
                ?.trim('"') ?: return emptySet(),
            type = type,
            position = this.determinePosition()
        )

        return if (test.type == TestType.CONTAINER) {
            setOf(test) + this.lambdaArguments
                .mapNotNull { it.getLambdaExpression() }
                .flatMap { it.findTests() }
                .map { discoveredTest -> discoveredTest.copy(id = "${test.id}::${discoveredTest.id}") }
        } else {
            setOf(test)
        }
    }

    private fun KtLambdaExpression.findTests(): Set<DiscoveredTest> =
        this.bodyExpression
            ?.children
            ?.filterIsInstance<KtCallExpression>()
            ?.flatMap { callExpression -> callExpression.findTests() }
            ?.toSet()
            .orEmpty()

    override fun discoverTests(lambda: KtLambdaExpression): Set<DiscoveredTest> =
        lambda.findTests()
}