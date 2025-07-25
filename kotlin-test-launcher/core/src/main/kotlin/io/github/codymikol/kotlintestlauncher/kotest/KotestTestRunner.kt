package io.github.codymikol.kotlintestlauncher.kotest

import io.github.codymikol.kotlintestlauncher.TestFrameworkRunner
import io.github.codymikol.kotlintestlauncher.TestRunResult
import io.kotest.common.KotestInternal
import io.kotest.core.spec.Spec
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.listener.CompositeTestEngineListener
import io.kotest.engine.listener.PinnedSpecTestEngineListener
import io.kotest.engine.listener.ThreadSafeTestEngineListener
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

internal object KotestTestRunner : TestFrameworkRunner {
    override fun isRunnable(kclass: KClass<*>): Boolean = kclass.isSubclassOf(Spec::class)

    /**
     * Heavily influenced by [Kotest launcher main.kt](https://github.com/kotest/kotest/blob/b98f125bd9f2efe592e9e69faa082f4ba11a8c22/kotest-framework/kotest-framework-engine/src/jvmMain/kotlin/io/kotest/engine/launcher/main.kt)
     */
    @OptIn(KotestInternal::class)
    override suspend fun run(classes: Collection<KClass<*>>): TestRunResult {
        val reporter = KotestTestReporter()

        @Suppress("UNCHECKED_CAST") // safe because [isRunnable] ensures that this is a KClass<out Spec>
        val result =
            TestEngineLauncher(
                CompositeTestEngineListener(
                    listOf(
                        ThreadSafeTestEngineListener(PinnedSpecTestEngineListener(reporter)),
                    ),
                ),
            ).withClasses(classes.toList() as List<KClass<out Spec>>)
                .async()

        return if (result.errors.isNotEmpty()) TestRunResult.Failure else TestRunResult.Success(report = reporter.report())
    }
}
