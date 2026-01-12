package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.model.Discovered
import org.jetbrains.kotlin.psi.KtExpression

internal interface KotestExpressionTestTypeDiscoverer : KotestTestTypeDiscoverer {
    fun discoverTests(
        expression: KtExpression?,
        classFqn: String,
    ): Set<Discovered>
}
