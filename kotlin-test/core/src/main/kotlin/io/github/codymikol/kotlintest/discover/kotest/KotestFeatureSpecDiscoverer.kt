package io.github.codymikol.kotlintest.discover.kotest

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#feature-spec)
 */
internal object KotestFeatureSpecDiscoverer : KotestKtExpressionDiscoverer() {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.getAllSuperClasses().any { fqn ->
            fqn == FqName("io.kotest.core.spec.style.FeatureSpec")
        }

    override val containers: List<String> = listOf("feature")
    override val tests: List<String> = listOf("scenario")
}
