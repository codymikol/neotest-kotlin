package io.github.codymikol.kotlintest.discover.junit

import com.intellij.psi.util.childrenOfType
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.DiscoveredResult
import io.github.codymikol.kotlintest.discover.model.determinePosition
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.kotlin.resolve.calls.util.getValueArgumentsInParentheses

/**
 * Supports JUnit 5 and 6 test discovery.
 *
 * [docs](https://docs.junit.org/6.0.1/overview.html)
 */
internal object JUnitTestDiscoverer : TestDiscoverer {
    private fun KtAnnotated.isDisabled(): Boolean =
        this.annotationEntries.flatMap { it.getAllSuperAnnotations() }.any {
            it == FqName("org.junit.jupiter.api.Disabled")
        } ||
            // HACK: fallback to just checking for '@Ignore' this catches
            // cases where JUnit changes the FQN for '@Ignore' and it also handles
            // the case of kotlin.test where you can't mock and define custom definitions
            // because it's part of 'kotlin.*' package.
            this.annotationEntries.any { it.shortName?.identifier == "Ignore" }

    /**
     * [reference](https://docs.junit.org/6.0.0/api/org.junit.jupiter.api/org/junit/jupiter/api/Test.html)
     */
    private fun KtFunction.isTest(): Boolean =
        !this.isPrivate() &&
            !isDisabled() &&
            (
                this.annotationEntries.flatMap { it.getAllSuperAnnotations() }.any {
                    (!this.hasDeclaredReturnType() && it == FqName("org.junit.jupiter.api.Test")) ||
                        it == FqName("org.junit.jupiter.api.TestFactory") ||
                        it == FqName("org.junit.jupiter.api.RepeatedTest") ||
                        it == FqName("org.junit.jupiter.api.TestTemplate") ||
                        it == FqName("org.junit.jupiter.params.ParameterizedTest")
                } ||
                    // HACK: fallback to just checking for '@Test' this catches
                    // cases where JUnit changes the FQN for '@Test' and it also handles
                    // the case of kotlin.test where you can't mock and define custom definitions
                    // because it's part of 'kotlin.*' package.
                    this.annotationEntries.any { it.shortName?.identifier == "Test" }
                )

    /**
     * [reference](https://docs.junit.org/6.0.0/api/org.junit.jupiter.api/org/junit/jupiter/api/Nested.html)
     */
    private fun KtClass.isTest(): Boolean =
        this.isInner() &&
            this.annotationEntries
                .flatMap { it.getAllSuperAnnotations() }
                .any { it == FqName("org.junit.jupiter.api.Nested") } &&
            !this.isDisabled()

    private fun KtAnnotated.determineDisplayName(): String? =
        this
            .annotationEntries
            .firstOrNull { annotation ->
                annotation.getAllSuperAnnotations().any {
                    it == FqName("org.junit.jupiter.api.DisplayName")
                }
            }
            ?.getValueArgumentsInParentheses()
            ?.firstOrNull()
            ?.getArgumentExpression()
            ?.text
            ?.trim('"')

    private fun KtClass.discoverTests(parentId: String): Set<Discovered> =
        this.body
            ?.functions
            ?.filter { function -> function.isTest() }
            ?.mapNotNull { function ->
                val name = function.determineDisplayName() ?: function.name ?: return@mapNotNull null

                Discovered.Test(
                    id = "$parentId::$name",
                    name = name,
                    position = function.determinePosition(),
                )
            }?.toSet()
            .orEmpty() +
            this.body
                ?.childrenOfType<KtClass>()
                ?.filter { clazz -> clazz.isTest() }
                ?.mapNotNull { clazz ->
                    val name = clazz.determineDisplayName() ?: clazz.name ?: return@mapNotNull null
                    val fullId = "$parentId::$name"

                    Discovered.Container(
                        id = fullId,
                        name = name,
                        position = clazz.determinePosition(),
                        tests = clazz.discoverTests(fullId),
                    )
                }?.toSet()
                .orEmpty()

    override fun discoverTests(kotlinFile: KtFile): DiscoveredResult {
        val classes = kotlinFile.childrenOfType<KtClass>()

        return DiscoveredResult(
            tests = classes
                .filterNot { clazz -> clazz.isDisabled() }
                .mapNotNull { clazz ->
                    val classFqn = clazz.fqName?.asString() ?: return@mapNotNull null

                    Discovered.Container(
                        id = classFqn,
                        name = checkNotNull(clazz.determineDisplayName() ?: clazz.name),
                        position = clazz.determinePosition(),
                        tests = clazz.discoverTests(classFqn),
                    )
                }
                .filter { it.tests.isNotEmpty() }
                .toSet()
        )
    }
}

/**
 * Recursively returns the list of annotations on this [KtAnnotationEntry].
 *
 * [original code](https://github.com/kotest/kotest-intellij-plugin/blob/4f27fa2f057509a72f219eeb5140902603815839/src/main/kotlin/io/kotest/plugin/intellij/psi/superClasses.kt#L9-L24)
 */
internal fun KtAnnotationEntry.getAllSuperAnnotations(): List<FqName> {
    val ref = this.typeReference

    return analyze(this) {
        val symbol = ref?.type?.symbol ?: return emptyList()

        fun KaClassLikeSymbol.getAllSuperAnnotations(): List<FqName> =
            listOfNotNull(this.classId?.asSingleFqName()) + this.annotations
                .mapNotNull { it.constructorSymbol?.returnType?.symbol }
                .flatMap { it.getAllSuperAnnotations() }

        symbol.getAllSuperAnnotations()
    }
}
