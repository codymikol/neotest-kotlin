package io.github.codymikol.neotestkotlin.framework

import io.github.codymikol.neotestkotlin.framework.kotest.KotestTestRunner
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

/**
 * Base interface for all test framework runners.
 */
interface TestFrameworkRunner {
    /**
     * Runs the provided test [classes] using the [TestFrameworkRunner].
     */
    suspend fun run(classes: Collection<KClass<*>>): TestRunResult

    /**
     * Whether this class is runnable by this [TestFrameworkRunner].
     */
    fun isRunnable(kclass: KClass<*>): Boolean

    companion object {
        /**
         * Executes all tests using all supported [TestFrameworkRunner]s
         * generating a [RunReport] that contains all classes and their corresponding test
         * statuses.
         */
        suspend fun runAll(classes: List<KClass<*>>): RunReport =
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

typealias RunReport = Set<TestNode.Container>

sealed interface TestRunResult {
    data class Success(
        val report: RunReport,
    ) : TestRunResult

    object Failure : TestRunResult
}
