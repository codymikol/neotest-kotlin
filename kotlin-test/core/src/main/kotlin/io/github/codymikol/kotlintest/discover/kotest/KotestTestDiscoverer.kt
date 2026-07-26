package io.github.codymikol.kotlintest.discover.kotest

import com.intellij.psi.util.childrenOfType
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.disambiguateDuplicateNames
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.DiscoveredResult
import io.github.codymikol.kotlintest.discover.model.TestWarning
import io.github.codymikol.kotlintest.discover.model.determinePosition
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.isAbstract

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

    /**
     * Focus can only be applied to top-level tests.
     *
     * [docs](https://kotest.io/docs/framework/conditional/conditional-tests-with-focus-and-bang.html#focus)
     */
    private fun Collection<Discovered.Container>.focusWarnings(): List<TestWarning> = this
        .flatMap { it.allTests() }
        .filter { it.isNested() }
        .filter { it.name.startsWith("f:") }
        .map { test ->
            TestWarning(
                message = "Only top-level tests can use focus 'f:'",
                position = test.position
            )
        }

    /**
     * Bang always skips the test
     *
     * [docs](https://kotest.io/docs/framework/conditional/conditional-tests-with-focus-and-bang.html#bang)
     */
    private fun Collection<Discovered.Container>.bangWarnings(): List<TestWarning> =
        this
            .flatMap { it.allTests() }
            .filter { it.name.startsWith("!") }
            .map { test ->
                TestWarning(
                    message = "Test is always skipped because of '!' (bang) prefix",
                    position = test.position
                )
            }

    /**
     * Associates all classes/objects to the discoverers and the super types
     * that they can operate on.
     */
    private fun KtFile.classOrObjectsToDiscoverableSuperTypes():
        Map<KtClassOrObject, Map<KotestTestTypeDiscoverer, List<KtSuperTypeListEntry>>> =
        this
            .childrenOfType<KtClassOrObject>()
            .filter { it is KtObjectDeclaration || (it is KtClass && !it.isAbstract()) }
            .associateWith { kotlinClass -> kotlinClass.superTypeListEntries }
            .mapValues { (_, superTypes) ->
                testTypes.associateWith { testType ->
                    superTypes.filter { testType.canHandle(it) }
                }
            }

    override fun discoverTests(kotlinFile: KtFile): DiscoveredResult =
        analyze(kotlinFile) {
            val classOrObjectToTestTypeToSuperTypes = kotlinFile.classOrObjectsToDiscoverableSuperTypes()

            val tests = classOrObjectToTestTypeToSuperTypes
                .filter { (kotlinClass, _) -> kotlinClass.fqName?.asString() != null }
                .flatMap { (kotlinClass, testTypeToSuperTypes) ->
                    val classFqn = checkNotNull(kotlinClass.fqName?.asString())

                    testTypeToSuperTypes
                        .filterValues { superTypes -> superTypes.isNotEmpty() }
                        .map { (testType, superTypes) ->
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

                                    val initBlockTests = kotlinClass
                                        .body
                                        ?.anonymousInitializers
                                        ?.flatMap { initializer ->
                                            testType.discoverTests(initializer.body, classFqn)
                                        }
                                        ?.toSet()
                                        .orEmpty()

                                    val container = Discovered.Container(
                                        id = classFqn,
                                        position = kotlinClass.determinePosition(),
                                        name = checkNotNull(kotlinClass.name),
                                        tests = bodyConstructorTests + initBlockTests
                                    )

                                    container.disambiguateDuplicateNames()
                                }
                                is KotestClassBodyTestTypeDiscoverer -> {
                                    val container = Discovered.Container(
                                        id = classFqn,
                                        position = kotlinClass.determinePosition(),
                                        name = checkNotNull(kotlinClass.name),
                                        tests = testType.discoverTests(kotlinClass.body, classFqn),
                                    )

                                    container.disambiguateDuplicateNames()
                                }
                            }
                        }
                }.toSet()

            DiscoveredResult(
                tests = tests,
                warnings = tests.focusWarnings() + tests.bangWarnings()
            )
        }
}
