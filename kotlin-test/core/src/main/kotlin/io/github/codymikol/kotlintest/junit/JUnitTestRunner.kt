package io.github.codymikol.kotlintest.junit

import io.github.codymikol.kotlintest.TestFrameworkRunner
import io.github.codymikol.kotlintest.TestRunResult
import org.junit.platform.commons.annotation.Testable
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

    override suspend fun run(classes: Collection<KClass<*>>): TestRunResult {
        val selectedClasses = classes.map { DiscoverySelectors.selectClass(it.java) }

        val request =
            LauncherDiscoveryRequestBuilder
                .request()
                .selectors(selectedClasses)
                .build()

        val reporter = JUnitTestReporter()
        LauncherFactory
            .create()
            .apply { registerTestExecutionListeners(reporter) }
            .execute(request)

        return TestRunResult.Success(reporter.report())
    }
}
