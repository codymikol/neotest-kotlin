package io.github.codymikol.kotlintest.kotest.discoverers

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.determinePosition
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

    override fun discoverTests(lambda: KtLambdaExpression): Set<DiscoveredTest> =
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

                DiscoveredTest(
                    id = func.name ?: return@mapNotNull null,
                    position = func.determinePosition(),
                    type = TestType.TEST
                )
            }
            ?.toSet()
            .orEmpty()
}
