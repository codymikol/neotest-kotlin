package io.github.codymikol.kotlintestlauncher

import io.github.codymikol.kotlintestlauncher.kotest.KotestTestRunner
import io.kotest.common.runBlocking
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

/**
 * Base interface for all test framework runners.
 */
public interface TestFrameworkRunner {
    /**
     * Runs the provided test [classes] using the [TestFrameworkRunner].
     */
    public suspend fun run(classes: Collection<KClass<*>>): TestRunResult

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
        public fun runAll(classes: List<KClass<*>>): RunReport =
            runBlocking {
                flowOf<TestFrameworkRunner>(
                    KotestTestRunner,
                ).map { runner ->
                    val runnableClasses = classes.filter { runner.isRunnable(it) }

                    val result = runner.run(runnableClasses)
                    when (result) {
                        is TestRunResult.Success -> result.report
                        is TestRunResult.Failure -> TODO()
                    }
                }.fold(emptySet()) { acc, it -> acc + it }
            }
    }
}

public typealias RunReport = Set<TestNode.Container>

public sealed interface TestRunResult {
    public data class Success(
        val report: RunReport,
    ) : TestRunResult

    public object Failure : TestRunResult
}
