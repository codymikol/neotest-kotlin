package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.determinePosition
import io.kotest.core.spec.style.AnnotationSpec
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import org.jetbrains.kotlin.util.isAnnotated
import kotlin.collections.orEmpty

/**
 * [docs](https://kotest.io/docs/next/framework/testing-styles.html#annotation-spec)
 */
internal object KotestAnnotationSpecDiscoverer : KotestTestTypeDiscoverer {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.typeReference?.getTypeText() == AnnotationSpec::class.java.simpleName

    override fun discoverTests(
        lambda: KtLambdaExpression,
        classFqn: String,
    ): Set<Discovered> =
        lambda.bodyExpression
            ?.children
            ?.filterIsInstance<KtFunction>()
            ?.filter { it.isAnnotated }
            ?.mapNotNull { func ->
                if (func.annotationEntries.none { annotation -> annotation.shortName?.identifier == "Test" } ||
                    func.annotationEntries.any { annotation -> annotation.shortName?.identifier == "Ignore" }
                ) {
                    return@mapNotNull null
                }

                val id = func.name ?: return@mapNotNull null

                Discovered.Test(
                    id = "$classFqn::$id",
                    name = id,
                    position = func.determinePosition(),
                )
            }?.toSet()
            .orEmpty()
}
