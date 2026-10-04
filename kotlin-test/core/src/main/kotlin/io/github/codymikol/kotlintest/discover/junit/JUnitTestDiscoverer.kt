package io.github.codymikol.kotlintest.discover.junit

import com.intellij.psi.util.childrenOfType
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.disambiguatedNames
import io.github.codymikol.kotlintest.discover.duplicateNameWarnings
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.DiscoveredResult
import io.github.codymikol.kotlintest.discover.model.Position
import io.github.codymikol.kotlintest.discover.model.TestWarning
import io.github.codymikol.kotlintest.discover.model.determinePosition
import io.github.codymikol.kotlintest.discover.sourceSuperclasses
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassLikeSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.lexer.KtTokens
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

    private fun KtClass.isAbstractOrInterface(): Boolean =
        this.hasModifier(KtTokens.ABSTRACT_KEYWORD) || this.isInterface()

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

    /**
     * A test method or `@Nested` class ([nestedClass]) before its [Discovered] is created, since
     * its final name depends on its siblings.
     */
    private data class Candidate(
        val displayName: String,
        val key: JUnitSiblingKey,
        val position: Position,
        val nestedClass: KtClass? = null,
    )

    /**
     * Test methods and `@Nested` classes of this class, including those inherited from its
     * superclasses declared in source (e.g. an abstract base class), as JUnit runs these as part
     * of this class. Inherited test methods that are overridden aren't included, JUnit only runs
     * the overriding method when it is a test itself.
     */
    private fun KtClass.candidates(): List<Candidate> {
        val hierarchy = listOf(this) + this.sourceSuperclasses()
        val overridden = mutableSetOf<Pair<String?, Int>>()

        val methods = hierarchy
            .flatMap { clazz ->
                clazz.body
                    ?.functions
                    .orEmpty()
                    .filterNot { function -> function.isPrivate() }
                    // a method of a subclass overrides the method of a superclass with the same signature
                    .filter { function -> overridden.add(function.name to function.valueParameters.size) }
            }
            .filter { function -> function.isTest() }
            .mapNotNull { function ->
                val functionName = function.name ?: return@mapNotNull null

                Candidate(
                    displayName = function.determineDisplayName() ?: functionName,
                    key = JUnitSiblingKey.method(functionName, function.valueParameters.size),
                    position = function.determinePosition(),
                )
            }

        val nestedClasses = hierarchy
            .flatMap { clazz -> clazz.body?.childrenOfType<KtClass>().orEmpty() }
            .filter { clazz -> clazz.isTest() }
            .mapNotNull { clazz ->
                val className = clazz.name ?: return@mapNotNull null

                Candidate(
                    displayName = clazz.determineDisplayName() ?: className,
                    key = JUnitSiblingKey.nestedClass(className),
                    position = clazz.determinePosition(),
                    nestedClass = clazz,
                )
            }

        return methods + nestedClasses
    }

    /**
     * Discovers all tests in this class, appending `#1`, `#2`, ... to siblings that share a display
     * name. JUnit doesn't execute tests in source order, so duplicates are numbered in
     * [JUnitSiblingKey] order which the runner's `JUnitTestReporter`
     * reproduces when reporting results.
     */
    private fun KtClass.discoverTests(parentId: String): Set<Discovered> {
        val candidates = this.candidates()
        val sortedIndexes = candidates.indices.sortedBy { candidates[it].key }
        val names = sortedIndexes
            .zip(sortedIndexes.disambiguatedNames { candidates[it].displayName })
            .toMap()

        return candidates
            .mapIndexed { index, candidate ->
                val name = checkNotNull(names[index])
                val id = "$parentId::$name"

                if (candidate.nestedClass == null) {
                    Discovered.Test(id = id, name = name, position = candidate.position)
                } else {
                    Discovered.Container(
                        id = id,
                        name = name,
                        position = candidate.position,
                        tests = candidate.nestedClass.discoverTests(id),
                    )
                }
            }
            .toSet()
    }

    /**
     * Warns about sibling tests that share a display name in this class and its `@Nested` classes.
     */
    private fun KtClass.duplicateNameWarnings(): List<TestWarning> {
        val candidates = this.candidates()

        return duplicateNameWarnings(
            candidates.map { Discovered.Test(id = it.displayName, name = it.displayName, position = it.position) },
        ) + candidates.mapNotNull { it.nestedClass }.flatMap { it.duplicateNameWarnings() }
    }

    override fun discoverTests(kotlinFile: KtFile): DiscoveredResult {
        val classes = kotlinFile.childrenOfType<KtClass>()

        // abstract classes can't be run themselves, their tests are discovered as part of their subclasses
        val enabledClasses = classes.filterNot { clazz -> clazz.isDisabled() || clazz.isAbstractOrInterface() }

        return DiscoveredResult(
            tests = enabledClasses
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
                .toSet(),
            warnings = enabledClasses.flatMap { clazz -> clazz.duplicateNameWarnings() },
        )
    }
}

/**
 * Recursively returns the list of annotations on this [KtAnnotationEntry], including itself.
 * Every annotation is returned at most once.
 *
 * [original code](https://github.com/kotest/kotest-intellij-plugin/blob/4f27fa2f057509a72f219eeb5140902603815839/src/main/kotlin/io/kotest/plugin/intellij/psi/superClasses.kt#L9-L24)
 */
internal fun KtAnnotationEntry.getAllSuperAnnotations(): List<FqName> {
    val ref = this.typeReference

    return analyze(this) {
        val symbol = ref?.type?.symbol ?: return emptyList()

        // Annotations can be (indirectly) annotated with themselves, e.g. `@Retention` and `@Documented`
        val visited = mutableSetOf<FqName>()

        fun KaClassLikeSymbol.getAllSuperAnnotations(): List<FqName> {
            val fqName = this.classId?.asSingleFqName()?.takeIf { visited.add(it) } ?: return emptyList()

            return listOf(fqName) + this.annotations
                .mapNotNull { it.constructorSymbol?.returnType?.symbol }
                .flatMap { it.getAllSuperAnnotations() }
        }

        symbol.getAllSuperAnnotations()
    }
}
