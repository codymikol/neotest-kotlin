package io.github.codymikol.kotlintest.kotest.discoverers

import io.kotest.core.spec.style.ExpectSpec
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#expect-spec)
 */
internal object KotestExpectSpecDiscoverer : KotestKtExpressionDiscoverer() {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == ExpectSpec::class.java.simpleName

    override val containers: List<String> = listOf("context")
    override val tests: List<String> = listOf("expect")
}