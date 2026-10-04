package io.github.codymikol.kotlintest.execute

import io.github.codymikol.kotlintest.execute.junit.JUnitTestExecutor
import io.github.codymikol.kotlintest.execute.kotest.KotestTestExecutor
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Base interface for all test framework runners.
 */
public interface TestFrameworkExecutor {
    /**
     * Runs the provided test [classes] using the [TestFrameworkExecutor].
     */
    public suspend fun run(
        classes: Collection<KClass<*>>,
        filter: String? = null,
    ): TestRunResult

    /**
     * Whether this class is runnable by this [TestFrameworkExecutor].
     */
    public fun isRunnable(kclass: KClass<*>): Boolean

    public companion object {
        /**
         * Executes all tests using all supported [TestFrameworkExecutor]s
         * generating a [RunReport] that contains all classes and their corresponding test
         * statuses.
         */
        public fun runAll(
            classes: Set<KClass<*>>,
            filter: String? = null,
        ): RunReport = runAll(classes, filter, listOf(KotestTestExecutor, JUnitTestExecutor))

        internal fun runAll(
            classes: Set<KClass<*>>,
            filter: String?,
            executors: List<TestFrameworkExecutor>,
        ): RunReport =
            runBlocking {
                executors.fold(emptySet()) { acc, runner ->
                    val runnableClasses = classes.filter { runner.isRunnable(it) }

                    if (runnableClasses.isEmpty()) {
                        return@fold acc
                    }

                    acc +
                        when (val result = runner.run(runnableClasses, filter)) {
                            is TestRunResult.Success -> result.report
                            is TestRunResult.Failure -> result.toReport(runnableClasses)
                        }
                }
            }
    }
}

public typealias RunReport = Set<TestResult>

public sealed interface TestRunResult {
    public data class Success(
        val report: RunReport,
    ) : TestRunResult

    /**
     * The test engine itself failed, as opposed to individual tests failing.
     */
    public data class Failure(
        /**
         * The errors reported by the test engine.
         */
        val errors: List<Throwable>,
        /**
         * Results of any tests that were executed before or despite the engine failure.
         */
        val report: RunReport = emptySet(),
    ) : TestRunResult
}

/**
 * Creates a [RunReport] containing the partial [TestRunResult.Failure.report] and a class level
 * failure with the engine errors for every requested class that does not already have one, so the
 * engine failure is visible for each requested class.
 */
internal fun TestRunResult.Failure.toReport(classes: Collection<KClass<*>>): RunReport {
    val classesWithClassLevelResult = report.filter { it.id.isEmpty() }.map { it.className }.toSet()
    val status = TestStatus.Failure.fromEngineErrors(errors)

    return report +
        classes
            .map { it.qualifiedName ?: it.java.name }
            .filterNot { it in classesWithClassLevelResult }
            .map { className ->
                TestResult(
                    className = className,
                    id = "",
                    duration = Duration.ZERO,
                    status = status,
                )
            }
}
