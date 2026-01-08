package io.github.codymikol.kotlintest.discover.kotest

import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

internal sealed interface KotestTestTypeDiscoverer {
    /**
     * Whether this [KotestTestTypeDiscoverer] can handle this SuperType
     *
     * e.g., FunSpec
     */
    fun canHandle(superType: KtSuperTypeListEntry): Boolean
}
