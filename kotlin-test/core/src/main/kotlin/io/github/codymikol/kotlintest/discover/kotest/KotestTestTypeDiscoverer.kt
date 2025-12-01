package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.DiscoveredTest
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

internal sealed interface KotestTestTypeDiscoverer {
    /**
     * Whether this [KotestTestTypeDiscoverer] can handle this SuperType
     *
     * e.g., FunSpec
     */
    fun canHandle(superType: KtSuperTypeListEntry): Boolean

    fun discoverTests(lambda: KtLambdaExpression): Set<DiscoveredTest>
}
