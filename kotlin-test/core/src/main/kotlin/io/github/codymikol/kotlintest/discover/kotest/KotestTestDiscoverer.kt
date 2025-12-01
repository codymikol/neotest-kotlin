package io.github.codymikol.kotlintest.discover.kotest

import com.intellij.psi.util.childrenOfType
import io.github.codymikol.kotlintest.discover.DiscoveredTest
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.TestType
import io.github.codymikol.kotlintest.discover.determinePosition
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.containingClass

internal object KotestTestDiscoverer : TestDiscoverer {
    internal val testTypes: List<KotestTestTypeDiscoverer> = listOf(
        KotestFunSpecDiscoverer,
        KotestBehaviorSpecDiscoverer,
        KotestShouldSpecDiscoverer,
        KotestDescribeSpecDiscoverer,
        KotestFeatureSpecDiscoverer,
        KotestExpectSpecDiscoverer,
        KotestStringSpecDiscoverer,
        KotestWordSpecDiscoverer,
        KotestFreeSpecDiscoverer,
        KotestAnnotationSpecDiscoverer
    )

    override fun discoverTests(kotlinFile: KtFile): Set<DiscoveredTest> = analyze(kotlinFile) {
        val superTypes = kotlinFile
            .childrenOfType<KtClass>()
            .flatMap { kotlinClass -> kotlinClass.superTypeListEntries }

        val testTypeToSuperTypes = testTypes.associateWith { testType ->
            superTypes.filter { testType.canHandle(it) }
        }

        testTypeToSuperTypes.flatMap { (testType, superTypes) ->
            superTypes
                .mapNotNull { entry -> entry.lastChild as? KtValueArgumentList }
                .flatMap { argumentList -> argumentList.arguments }
                .flatMap { argument -> argument.children.toList() }
                .filterIsInstance<KtLambdaExpression>()
                .flatMap { lambda ->
                    val classFqn = lambda.containingClass()?.fqName?.asString() ?: return@flatMap emptyList()

                    setOf(
                        DiscoveredTest(
                            id = classFqn,
                            position = checkNotNull(lambda.containingClass()).determinePosition(),
                            type = TestType.CONTAINER
                        )
                    ) + testType
                        .discoverTests(lambda)
                        .map { test -> test.copy(id = "$classFqn::${test.id}") }
                }
        }.toSet()
    }
}
