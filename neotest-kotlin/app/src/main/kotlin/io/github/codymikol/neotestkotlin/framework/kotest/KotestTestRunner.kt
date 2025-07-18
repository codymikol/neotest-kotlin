package io.github.codymikol.neotestkotlin.framework.kotest

import io.github.codymikol.neotestkotlin.framework.TestFrameworkRunner
import io.github.codymikol.neotestkotlin.framework.TestRunResult
import io.kotest.common.KotestInternal
import io.kotest.common.runBlocking
import io.kotest.core.spec.Spec
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.listener.CollectingTestEngineListener
import io.kotest.engine.listener.CompositeTestEngineListener
import io.kotest.engine.listener.LoggingTestEngineListener
import io.kotest.engine.listener.PinnedSpecTestEngineListener
import io.kotest.engine.listener.ThreadSafeTestEngineListener
import kotlin.reflect.KClass

object KotestTestRunner : TestFrameworkRunner {
    override fun isRunnable(kclass: KClass<*>): Boolean = kclass.isInstance(Spec::class)

    @OptIn(KotestInternal::class)
    override fun run(classes: Collection<KClass<*>>): TestRunResult {
        val collector = CollectingTestEngineListener()
        val reporter = KotestTestReporter()

        runBlocking {
            TestEngineLauncher(
                CompositeTestEngineListener(
                    listOf(
                        collector,
                        LoggingTestEngineListener,
                        ThreadSafeTestEngineListener(PinnedSpecTestEngineListener(reporter)),
                    ),
                ),
            ).withClasses(classes.toList() as List<KClass<out Spec>>)
                .async()
        }

        return if (collector.errors) TestRunResult.Failure else TestRunResult.Success(report = reporter.report())
    }
}
