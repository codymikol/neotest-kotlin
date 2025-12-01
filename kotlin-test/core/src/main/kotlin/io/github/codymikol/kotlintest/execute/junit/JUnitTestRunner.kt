package io.github.codymikol.kotlintest.execute.junit

import io.github.codymikol.kotlintest.execute.TestFrameworkRunner
import io.github.codymikol.kotlintest.execute.TestRunResult
import org.junit.platform.commons.annotation.Testable
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

internal object JUnitTestRunner : TestFrameworkRunner {
    override fun isRunnable(kclass: KClass<*>): Boolean =
        kclass.memberFunctions
            .flatMap { it.annotations }
            .any { it.annotationClass.findAnnotation<Testable>() != null }

    override suspend fun run(
        classes: Collection<KClass<*>>,
        filter: String?,
    ): TestRunResult {
        val request =
            LauncherDiscoveryRequestBuilder
                .request()
                .selectors(classes.toJUnitSelectors(filter))
                .build()

        val reporter = JUnitTestReporter()
        LauncherFactory
            .create()
            .apply { registerTestExecutionListeners(reporter) }
            .execute(request)

        return TestRunResult.Success(reporter.report())
    }
}

/**
 * Translates filters of the form
 *
 * ```
 * org.example.TestExample::namespace::nested namespace::test
 *
 * org.example.TestExample::namespace
 *
 * org.example.TestExample::test
 * ```
 *
 * to the proper JUnit DiscoverySelector
 *
 * ```
 * // nested namespace
 * DiscoverySelectors.selectNestedMethod(
 *   listOf(org.example.TestExample, org.example.TestExample$Namespace),
 *   org.example.TestExample$Namespace$NestedNamespace,
 *   "test"
 * )
 *
 * // top-level namespace
 * DiscoverySelectors.selectNestedClass(listOf(org.example.TestExample), org.example.TestExample$Namespace)
 *
 * // top-level test
 * DiscoverySelectors.selectMethod(org.example.TestExample, "test")
 * ```
 */
internal fun Collection<KClass<*>>.toJUnitSelectors(filter: String?): List<DiscoverySelector> {
    val selectedClasses = this.map { DiscoverySelectors.selectClass(it.java) }

    val parts = filter?.split("::")
    if (parts == null || parts.size == 1) {
        return selectedClasses
    }

    val selectedClass = this.firstOrNull { it.qualifiedName == parts.first() } ?: return selectedClasses
    val filteredClasses: List<Pair<String, Class<*>?>> =
        parts
            .drop(1)
            .runningFold(parts.first() to selectedClass as KClass<*>?) { (_, parent), className ->
                className to parent?.nestedClasses?.find { it.simpleName == className }
            }.map { (part, kotlinClass) -> part to kotlinClass?.java }

    return when {
        // ends with a class
        filteredClasses.last().second != null ->
            listOf(
                DiscoverySelectors.selectNestedClass(
                    filteredClasses.map { it.second }.dropLast(1),
                    checkNotNull(filteredClasses.last().second),
                ),
            )

        // nested classes ending with a test
        filteredClasses.last().second == null && filteredClasses.size > 2 ->
            listOf(
                DiscoverySelectors.selectNestedMethod(
                    filteredClasses.dropLast(2).map { it.second },
                    checkNotNull(filteredClasses[filteredClasses.lastIndex - 1].second),
                    checkNotNull(filteredClasses.last().first),
                ),
            )

        // top-level test
        else -> listOf(DiscoverySelectors.selectMethod(selectedClass.qualifiedName, filteredClasses.last().first))
    }
}
