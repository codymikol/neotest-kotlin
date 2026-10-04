package io.github.codymikol.kotlintest.execute.kotest

import io.github.codymikol.kotlintest.execute.TestFrameworkExecutor
import io.github.codymikol.kotlintest.execute.TestRunResult
import io.kotest.common.KotestInternal
import io.kotest.core.descriptors.Descriptor
import io.kotest.core.descriptors.DescriptorPaths
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.Spec
import io.kotest.core.spec.SpecRef
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.extensions.filter.IncludeDescriptorFilter
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

internal object KotestTestExecutor : TestFrameworkExecutor {
    override fun isRunnable(kclass: KClass<*>): Boolean = kclass.isSubclassOf(Spec::class) && !kclass.isAbstract

    /**
     * Heavily influenced by [Kotest launcher main.kt](https://github.com/kotest/kotest/blob/b98f125bd9f2efe592e9e69faa082f4ba11a8c22/kotest-framework/kotest-framework-engine/src/jvmMain/kotlin/io/kotest/engine/launcher/main.kt)
     */
    override suspend fun run(
        classes: Collection<KClass<*>>,
        filter: String?,
    ): TestRunResult = run(classes, filter, emptyList())

    /**
     * Runs the [classes] with additional Kotest [extensions].
     */
    @OptIn(KotestInternal::class)
    internal suspend fun run(
        classes: Collection<KClass<*>>,
        filter: String?,
        extensions: List<Extension>,
    ): TestRunResult {
        val reporter = KotestTestReporter()

        val filters = filter.toKotestFilters()

        // UNCHECKED_CAST is safe because [isRunnable] ensures that this is a KClass<out Spec>
        @Suppress("UNCHECKED_CAST", "SpreadOperator")
        val result =
            TestEngineLauncher()
                .withListener(reporter.engineListener)
                .withSpecRefs(
                    classes.map { SpecRef.Reference(it as KClass<out Spec>, it.java.name) },
                ).addExtensions(
                    listOfNotNull(
                        reporter,
                        filters.takeIf { it.isNotEmpty() }?.let { IncludeDescriptorFilter(*it.toTypedArray()) },
                    ) + extensions,
                ).execute()

        return if (result.errors.isNotEmpty()) {
            TestRunResult.Failure(
                errors = result.errors,
                report = reporter.report(),
            )
        } else {
            TestRunResult.Success(
                report = reporter.report(),
            )
        }
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
 *
 * Disambiguated duplicate names (`test#2`) are translated to the name that Kotest runs them as
 * (`(1) test`), see [toKotestNames], so a filter can result in multiple [Descriptor]s.
 */
@OptIn(KotestInternal::class)
internal fun String?.toKotestFilters(): List<Descriptor> {
    val parts = this?.split("::") ?: return emptyList()
    val className = parts.first()

    return parts
        .drop(1)
        .fold(listOf(emptyList<String>())) { paths, part ->
            paths.flatMap { path -> part.toKotestNames().map { name -> path + name } }
        }
        .map { path ->
            val kotestFilter = if (path.isEmpty()) {
                className
            } else {
                "$className/${path.joinToString(separator = " -- ")}"
            }

            DescriptorPaths.parse(kotestFilter)
        }
}
