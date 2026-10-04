package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.determinePosition
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#string-spec)
 */
internal object KotestStringSpecDiscoverer : KotestExpressionTestTypeDiscoverer {
    override val factoryBuilder: String = "stringSpec"

    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.getAllSuperClasses().any { fqn ->
            fqn == FqName("io.kotest.core.spec.style.StringSpec")
        }

    override fun discoverStatement(
        statement: KtExpression,
        parentId: String,
    ): List<Discovered> {
        val callExpression = statement as? KtCallExpression ?: return emptyList()
        val id = callExpression.firstChild.text.trim('"')

        return listOf(
            Discovered.Test(
                id = "$parentId::$id",
                name = id,
                position = callExpression.determinePosition(),
            ),
        )
    }
}
