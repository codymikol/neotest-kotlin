package io.github.codymikol.kotlintest.kotest.discoverers

import io.kotest.core.spec.style.FeatureSpec
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#feature-spec)
 */
internal object KotestFeatureSpecDiscoverer : KotestKtExpressionDiscoverer() {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == FeatureSpec::class.java.simpleName

    override val containers: List<String> = listOf("feature")
    override val tests: List<String> = listOf("scenario")
}