package io.github.codymikol.kotlintest.discover.kotest

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#describe-spec)
 */
internal object KotestDescribeSpecDiscoverer : KotestKtExpressionDiscoverer() {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.getAllSuperClasses().any { fqn ->
            fqn == FqName("io.kotest.core.spec.style.DescribeSpec")
        }

    override val containers: List<String> = listOf("describe")
    override val tests: List<String> = listOf("it")
    override val disambiguateDuplicateNames: Boolean = true
}
