package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.junit.JUnitTestRunner
import io.github.codymikol.kotlintest.kotest.KotestTestRunner
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

/**
 * Base interface for all test framework runners.
 */
public interface TestFrameworkRunner {
    /**
     * Runs the provided test [classes] using the [TestFrameworkRunner].
     */
    public suspend fun run(
        classes: Collection<KClass<*>>,
        filter: String? = null,
    ): TestRunResult

    /**
     * Whether this class is runnable by this [TestFrameworkRunner].
     */
    public fun isRunnable(kclass: KClass<*>): Boolean

    public companion object {
        /**
         * Executes all tests using all supported [TestFrameworkRunner]s
         * generating a [RunReport] that contains all classes and their corresponding test
         * statuses.
         */
        public fun runAll(
            classes: Set<KClass<*>>,
            filter: String? = null,
        ): RunReport =
            runBlocking {
                flowOf(
                    KotestTestRunner,
                    JUnitTestRunner,
                ).map { runner ->
                    val runnableClasses = classes.filter { runner.isRunnable(it) }

                    if (runnableClasses.isEmpty()) {
                        return@map emptySet()
                    }

                    require(filter == null || runnableClasses.any { filter.startsWith(checkNotNull(it.qualifiedName)) }) {
                        "filter must be prefixed with the fully qualified class name (fqcn), but was '$filter'"
                    }

                    when (val result = runner.run(runnableClasses, filter)) {
                        is TestRunResult.Success -> result.report
                        is TestRunResult.Failure -> TODO()
                    }
                }.fold(emptySet()) { acc, it -> acc + it }
            }
    }
}

public typealias RunReport = Set<TestResult>

public sealed interface TestRunResult {
    public data class Success(
        val report: RunReport,
    ) : TestRunResult

    public object Failure : TestRunResult
}
