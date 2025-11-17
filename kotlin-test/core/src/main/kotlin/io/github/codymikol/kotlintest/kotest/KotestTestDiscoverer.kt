package io.github.codymikol.kotlintest.kotest

import com.intellij.psi.util.childrenOfType
import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.TestDiscoverer
import io.github.codymikol.kotlintest.kotest.discoverers.KotestBehaviorSpecDiscoverer
import io.github.codymikol.kotlintest.kotest.discoverers.KotestDescribeSpecDiscoverer
import io.github.codymikol.kotlintest.kotest.discoverers.KotestExpectSpecDiscoverer
import io.github.codymikol.kotlintest.kotest.discoverers.KotestFeatureSpecDiscoverer
import io.github.codymikol.kotlintest.kotest.discoverers.KotestFunSpecDiscoverer
import io.github.codymikol.kotlintest.kotest.discoverers.KotestShouldSpecDiscoverer
import io.github.codymikol.kotlintest.kotest.discoverers.KotestTestTypeDiscoverer
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtValueArgumentList

internal object KotestTestDiscoverer : TestDiscoverer {
    internal val testTypes: List<KotestTestTypeDiscoverer> = listOf(
        KotestFunSpecDiscoverer,
        KotestBehaviorSpecDiscoverer,
        KotestShouldSpecDiscoverer,
        KotestDescribeSpecDiscoverer,
        KotestFeatureSpecDiscoverer,
        KotestExpectSpecDiscoverer
    )

    override fun discoverTests(kotlinFile: KtFile): Set<DiscoveredTest> = analyze(kotlinFile) {
        val superTypes = kotlinFile
                .childrenOfType<KtClass>()
                .flatMap { kotlinClass -> kotlinClass.superTypeListEntries }

        val testTypeToSuperTypes = testTypes.associateWith {
            testType -> superTypes.filter { testType.canHandle(it) }
        }

        testTypeToSuperTypes.flatMap { (testType, superTypes) ->
            superTypes
                .mapNotNull { entry -> entry.lastChild as? KtValueArgumentList }
                .flatMap { argumentList -> argumentList.arguments }
                .flatMap { argument -> argument.children.toList() }
                .filterIsInstance<KtLambdaExpression>()
                .flatMap { lambda ->
                    testType.discoverTests(lambda)
                }
        }.toSet()
    }
}
