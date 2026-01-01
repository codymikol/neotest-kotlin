package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.Discovered
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

internal sealed interface KotestTestTypeDiscoverer {
    /**
     * Whether this [KotestTestTypeDiscoverer] can handle this SuperType
     *
     * e.g., FunSpec
     */
    fun canHandle(superType: KtSuperTypeListEntry): Boolean

    fun discoverTests(
        lambda: KtLambdaExpression,
        classFqn: String,
    ): Set<Discovered>
}
