package io.github.codymikol.kotlintest.execute.kotest

import io.github.codymikol.kotlintest.execute.TestFrameworkExecutor
import io.github.codymikol.kotlintest.execute.TestRunResult
import io.kotest.common.KotestInternal
import io.kotest.core.descriptors.Descriptor
import io.kotest.core.descriptors.DescriptorPaths
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
    @OptIn(KotestInternal::class)
    override suspend fun run(
        classes: Collection<KClass<*>>,
        filter: String?,
    ): TestRunResult {
        val reporter = KotestTestReporter()

        @Suppress("UNCHECKED_CAST") // safe because [isRunnable] ensures that this is a KClass<out Spec>
        val result =
            TestEngineLauncher()
                .withSpecRefs(
                    classes.map { SpecRef.Reference(it as KClass<out Spec>, it.java.name) },
                ).addExtensions(
                    listOfNotNull(
                        reporter,
                        filter.toKotestFilter()?.let { IncludeDescriptorFilter(it) },
                    ),
                ).execute()

        return if (result.errors.isNotEmpty()) {
            TestRunResult.Failure
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
 */
@OptIn(KotestInternal::class)
internal fun String?.toKotestFilter(): Descriptor? =
    this?.replaceFirst("::", "/")?.replace("::", " -- ")?.let { kotestFilter ->
        DescriptorPaths.parse(kotestFilter)
    }
