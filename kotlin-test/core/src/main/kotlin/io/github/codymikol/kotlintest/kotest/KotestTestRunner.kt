package io.github.codymikol.kotlintest.kotest

import io.github.codymikol.kotlintest.TestFrameworkRunner
import io.github.codymikol.kotlintest.TestRunResult
import io.kotest.common.KotestInternal
import io.kotest.core.descriptors.Descriptor
import io.kotest.core.descriptors.DescriptorPaths
import io.kotest.core.spec.Spec
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.extensions.IncludeDescriptorFilter
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

internal object KotestTestRunner : TestFrameworkRunner {
    override fun isRunnable(kclass: KClass<*>): Boolean = kclass.isSubclassOf(Spec::class) && !kclass.isAbstract

    /**
     * Heavily influenced by [Kotest launcher main.kt](https://github.com/kotest/kotest/blob/b98f125bd9f2efe592e9e69faa082f4ba11a8c22/kotest-framework/kotest-framework-engine/src/jvmMain/kotlin/io/kotest/engine/launcher/main.kt)
     */
    @OptIn(KotestInternal::class)
    override suspend fun run(
        classes: Collection<KClass<*>>,
        filter: String?,
    ): TestRunResult {
        val reporter = KotestTestReporter()

        @Suppress("UNCHECKED_CAST") // safe because [isRunnable] ensures that this is a KClass<out Spec>
        val result =
            TestEngineLauncher()
                .withListener(reporter)
                .withClasses(classes.toList() as List<KClass<out Spec>>)
                .addExtensions(listOfNotNull(filter.toKotestFilter()?.let { IncludeDescriptorFilter(it) }))
                .async()

        return if (result.errors.isNotEmpty()) TestRunResult.Failure else TestRunResult.Success(report = reporter.report())
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
 * to the form
 *
 * ```
 * org.example.TestExample/namespace -- nested namespace -- test
 *
 * org.example.TestExample/namespace
 *
 * org.example.TestExample/test
 * ```
 */
internal fun String?.toKotestFilter(): Descriptor? =
    this?.replaceFirst("::", "/")?.replace("::", " -- ")?.let { kotestFilter ->
        DescriptorPaths.parse(kotestFilter)
    }
