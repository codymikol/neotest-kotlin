package io.github.codymikol.kotlintest.kotest.discoverers

import io.kotest.core.spec.style.DescribeSpec
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#describe-spec)
 */
internal object KotestDescribeSpecDiscoverer : KotestKtExpressionDiscoverer() {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == DescribeSpec::class.java.simpleName

    override val containers: List<String> = listOf("describe")
    override val tests: List<String> = listOf("it")
}
