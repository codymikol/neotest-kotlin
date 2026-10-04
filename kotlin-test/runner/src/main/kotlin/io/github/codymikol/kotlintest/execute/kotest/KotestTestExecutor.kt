package io.github.codymikol.kotlintest.execute.kotest

import io.github.codymikol.kotlintest.execute.TestFrameworkExecutor
import io.github.codymikol.kotlintest.execute.TestRunResult
import io.github.codymikol.kotlintest.execute.TestStatus
import io.github.codymikol.kotlintest.execute.isClassPresent
import io.kotest.common.KotestInternal
import io.kotest.core.descriptors.Descriptor
import io.kotest.core.descriptors.DescriptorPaths
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.Spec
import io.kotest.core.spec.SpecRef
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.extensions.filter.IncludeDescriptorFilter
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import io.kotest.engine.test.TestResult as KotestTestResult

internal object KotestTestExecutor : TestFrameworkExecutor {
    /**
     * Class used to detect whether Kotest is on the classpath at all.
     */
    const val SPEC_CLASS = "io.kotest.core.spec.Spec"

    /**
     * The oldest Kotest version this runner supports. Kotest 6.0 lacks
     * [TestEngineLauncher.execute] and [IncludeDescriptorFilter], Kotest 5 has an entirely
     * different engine API.
     */
    const val MINIMUM_VERSION = "6.1.0"

    /**
     * Classes that are only present in Kotest versions supported by this runner.
     */
    private val REQUIRED_CLASSES =
        listOf(
            "io.kotest.engine.test.TestResult",
            "io.kotest.engine.extensions.filter.IncludeDescriptorFilter",
        )

    override fun isRunnable(kclass: KClass<*>): Boolean =
        Spec::class.java.isAssignableFrom(kclass.java) && !Modifier.isAbstract(kclass.java.modifiers)

    /**
     * Fails with a descriptive message when the Kotest version on the classpath is unsupported,
     * rather than with a [LinkageError] somewhere inside the engine.
     */
    private fun checkSupportedVersion() {
        val missing = REQUIRED_CLASSES.filterNot { isClassPresent(it) }
        check(missing.isEmpty()) {
            val version = Spec::class.java.`package`?.implementationVersion ?: "unknown"
            "neotest-kotlin requires Kotest $MINIMUM_VERSION or newer, but the test classpath contains Kotest " +
                "$version (missing ${missing.joinToString()}). Please upgrade Kotest."
        }
    }

    /**
     * Heavily influenced by [Kotest launcher main.kt](https://github.com/kotest/kotest/blob/b98f125bd9f2efe592e9e69faa082f4ba11a8c22/kotest-framework/kotest-framework-engine/src/jvmMain/kotlin/io/kotest/engine/launcher/main.kt)
     */
    override fun run(
        classes: Collection<KClass<*>>,
        filter: String?,
    ): TestRunResult = run(classes, filter, emptyList())

    /**
     * Runs the [classes] with additional Kotest [extensions].
     */
    @OptIn(KotestInternal::class)
    internal fun run(
        classes: Collection<KClass<*>>,
        filter: String?,
        extensions: List<Extension>,
    ): TestRunResult {
        checkSupportedVersion()

        val reporter = KotestTestReporter()

        val filters = filter.toKotestFilters()

        // UNCHECKED_CAST is safe because [isRunnable] ensures that this is a KClass<out Spec>
        @Suppress("UNCHECKED_CAST", "SpreadOperator")
        val launcher =
            TestEngineLauncher()
                .withListener(reporter.engineListener)
                .withSpecRefs(
                    classes.map { SpecRef.Reference(it as KClass<out Spec>, it.java.name) },
                ).addExtensions(
                    listOfNotNull(
                        reporter,
                        filters.takeIf { it.isNotEmpty() }?.let { IncludeDescriptorFilter(*it.toTypedArray()) },
                    ) + extensions,
                )
        val result = runBlocking { launcher.execute() }

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

internal fun KotestTestResult.toTestStatus(): TestStatus =
    when (this) {
        is KotestTestResult.Success -> TestStatus.Success
        is KotestTestResult.Failure -> TestStatus.Failure.from(errorOrNull)
        is KotestTestResult.Ignored -> TestStatus.Ignored(reason = reason)
        // non-assertion exceptions, such as an exception thrown in the test or a NoSuchMethodError
        is KotestTestResult.Error -> TestStatus.Failure.from(errorOrNull)
    }
