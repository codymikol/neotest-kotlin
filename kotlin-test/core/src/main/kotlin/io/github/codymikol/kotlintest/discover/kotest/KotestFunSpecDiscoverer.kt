package io.github.codymikol.kotlintest.discover.kotest

import io.kotest.core.spec.style.FunSpec
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import org.jetbrains.kotlin.psi.psiUtil.isAbstract

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
