package io.github.codymikol.kotlintest.discover.kotest

import com.intellij.psi.util.childrenOfType
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.determinePosition
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.containingClass

internal object KotestTestDiscoverer : TestDiscoverer {
    internal val testTypes: List<KotestTestTypeDiscoverer> =
        listOf(
            KotestFunSpecDiscoverer,
            KotestBehaviorSpecDiscoverer,
            KotestShouldSpecDiscoverer,
            KotestDescribeSpecDiscoverer,
            KotestFeatureSpecDiscoverer,
            KotestExpectSpecDiscoverer,
            KotestStringSpecDiscoverer,
            KotestWordSpecDiscoverer,
            KotestFreeSpecDiscoverer,
            KotestAnnotationSpecDiscoverer,
        )

    override fun discoverTests(kotlinFile: KtFile): Set<Discovered.Container> =
        analyze(kotlinFile) {
            val superTypes =
                kotlinFile
                    .childrenOfType<KtClass>()
                    .flatMap { kotlinClass -> kotlinClass.superTypeListEntries }

            val testTypeToSuperTypes =
                testTypes.associateWith { testType ->
                    superTypes.filter { testType.canHandle(it) }
                }

            testTypeToSuperTypes
                .mapNotNull { (testType, superTypes) ->
                    val clazz = superTypes.firstOrNull()?.containingClass() ?: return@mapNotNull null
                    val classFqn = clazz.fqName?.asString() ?: return@mapNotNull null

                    when (testType) {
                        is KotestExpressionTestTypeDiscoverer -> {
                            val bodyConstructorTests = superTypes
                                .asSequence()
                                .mapNotNull { entry -> entry.lastChild as? KtValueArgumentList }
                                .flatMap { argumentList -> argumentList.arguments }
                                .flatMap { argument -> argument.children.toList() }
                                .filterIsInstance<KtLambdaExpression>()
                                .flatMap { lambda -> testType.discoverTests(lambda.bodyExpression, classFqn) }
                                .toSet()

                            val initBlockTests = clazz
                                .body
                                ?.anonymousInitializers
                                ?.flatMap { initializer ->
                                    testType.discoverTests(initializer.body, classFqn)
                                }
                                ?.toSet()
                                .orEmpty()

                            Discovered.Container(
                                id = classFqn,
                                position = clazz.determinePosition(),
                                name = checkNotNull(clazz.name),
                                tests = bodyConstructorTests + initBlockTests
                            )
                        }
                        is KotestClassBodyTestTypeDiscoverer -> {
                            Discovered.Container(
                                id = classFqn,
                                position = clazz.determinePosition(),
                                name = checkNotNull(clazz.name),
                                tests = testType.discoverTests(clazz.body, classFqn),
                            )
                        }
                        else -> error("unknown subtype for KotestTestTypeDiscoverer: ${testType::class}")
                    }
                }.toSet()
        }
}
