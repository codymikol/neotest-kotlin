package io.github.codymikol.kotlintest.kotest.discoverers

import io.kotest.core.spec.style.FunSpec
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#fun-spec)
 */
internal object KotestFunSpecDiscoverer : KotestKtExpressionDiscoverer() {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == FunSpec::class.java.simpleName

    override val containers: List<String> = listOf("context")
    override val tests: List<String> = listOf("test")
}
