package io.github.codymikol.kotlintest.discover.kotest

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#should-spec)
 */
internal object KotestShouldSpecDiscoverer : KotestKtExpressionDiscoverer() {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.getAllSuperClasses().any { fqn ->
            fqn == FqName("io.kotest.core.spec.style.ShouldSpec")
        }

    override val containers: List<String> = listOf("context")
    override val tests: List<String> = listOf("should")
}
