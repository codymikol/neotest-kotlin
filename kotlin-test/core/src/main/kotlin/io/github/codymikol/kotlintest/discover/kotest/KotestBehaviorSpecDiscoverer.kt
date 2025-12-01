package io.github.codymikol.kotlintest.discover.kotest

import io.kotest.core.spec.style.BehaviorSpec
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#behavior-spec)
 */
internal object KotestBehaviorSpecDiscoverer : KotestKtExpressionDiscoverer() {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == BehaviorSpec::class.java.simpleName

    override val containers: List<String> = listOf(
        "Context",
        "context",
        "Given",
        "given",
        "When",
        "`when`",
        "And",
        "and"
    )

    override val tests: List<String> = listOf(
        "Then",
        "then"
    )
}
