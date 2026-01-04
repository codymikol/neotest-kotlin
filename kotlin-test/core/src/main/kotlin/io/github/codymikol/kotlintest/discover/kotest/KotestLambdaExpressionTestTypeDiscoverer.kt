package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.Discovered
import org.jetbrains.kotlin.psi.KtLambdaExpression

internal interface KotestLambdaExpressionTestTypeDiscoverer : KotestTestTypeDiscoverer {
    fun discoverTests(
        lambda: KtLambdaExpression,
        classFqn: String,
    ): Set<Discovered>
}
