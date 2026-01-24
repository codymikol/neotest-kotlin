package io.github.codymikol.kotlintest.discover.kotest

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#fun-spec)
 */
internal object KotestFunSpecDiscoverer : KotestKtExpressionDiscoverer() {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.getAllSuperClasses().any { fqn ->
            fqn == FqName("io.kotest.core.spec.style.FunSpec")
        }

    override val containers: List<String> = listOf("context")
    override val tests: List<String> = listOf("test")
}
