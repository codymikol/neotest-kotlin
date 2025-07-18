package io.github.codymikol.neotestkotlin.framework.kotest
import io.github.codymikol.neotestkotlin.framework.ClassResult
import io.github.codymikol.neotestkotlin.framework.FullyQualifiedClassName
import io.github.codymikol.neotestkotlin.framework.TestResult
import io.github.codymikol.neotestkotlin.framework.TestResultStatus
import io.kotest.common.KotestInternal
import io.kotest.core.test.TestCase
import io.kotest.engine.interceptors.EngineContext
import io.kotest.engine.listener.TestEngineListener
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.time.toJavaDuration
import io.kotest.core.test.TestResult as KotestTestResult

/**
 * Implements Kotest's [TestEngineListener] for the sole purpose of observing spec/test completion
 * to create a [report].
 *
 * This reporter is thread safe, but generally should be instantiated once per execution.
 */
@OptIn(KotestInternal::class)
class KotestTestReporter : TestEngineListener {
    private val results: ConcurrentHashMap<FullyQualifiedClassName, ClassResult> = ConcurrentHashMap()

    fun report(): Map<FullyQualifiedClassName, ClassResult> = this.results.toMap()

    /**
     * Invoked as soon as the engine has been created.
     */
    override suspend fun engineStarted() {}

    /**
     * Invoked when the [TestEngine] has completed setup and is ready to begin
     * executing specs.
     *
     * @param context the final context that will be used.
     */
    override suspend fun engineInitialized(context: EngineContext) {}

    /**
     * Is invoked when the [TestEngine] has finished execution of all tests.
     *
     * If any unexpected errors were detected during execution then they will be
     * passed to this method.
     */
    override suspend fun engineFinished(t: List<Throwable>) {}

    /**
     * Invoked once per [Spec] to indicate that this spec will be instantiated
     * and any active tests invoked.
     */
    override suspend fun specStarted(kclass: KClass<*>) {
        val name = FullyQualifiedClassName(checkNotNull(kclass.qualifiedName))
        this.results[name] =
            ClassResult(
                name = name,
                result = TestResultStatus.Failure(cause = Throwable(message = "No spec results found for $name")),
                tests = emptyList(),
            )
    }

    /**
     * Invoked when a spec is ignored. An optional [reason] for being ignored can be provided.
     */
    override suspend fun specIgnored(
        kclass: KClass<*>,
        reason: String?,
    ) {
        val name = FullyQualifiedClassName(checkNotNull(kclass.qualifiedName))
        this.results[name] =
            ClassResult(
                name = name,
                result = TestResultStatus.Skipped(reason = reason),
                tests = emptyList(),
            )
    }

    /**
     * Is invoked once per [Spec] class to indicate this spec has completed.
     */
    override suspend fun specFinished(
        kclass: KClass<*>,
        result: KotestTestResult,
    ) {
        val name = FullyQualifiedClassName(checkNotNull(kclass.qualifiedName))
        this.results.compute(name) { _, currentValue ->
            checkNotNull(currentValue) {
                "specFinished event for class '$name' that hasn't been started."
            }

            currentValue.copy(result = result.toTestResultStatus())
        }
    }

    /**
     * Invoked if a [TestCase] is about to be executed.
     * Will not be invoked if the test is ignored.
     */
    override suspend fun testStarted(testCase: TestCase) {}

    /**
     * Invoked if a [TestCase] will be skipped.
     */
    override suspend fun testIgnored(
        testCase: TestCase,
        reason: String?,
    ) {
        val name = FullyQualifiedClassName(checkNotNull(testCase.spec.javaClass.kotlin.qualifiedName))
        this.results.compute(name) { _, currentValue ->
            checkNotNull(currentValue) {
                "testIgnored event for class '$name' and test '${testCase.name.testName}' that hasn't been started."
            }

            currentValue.copy(
                tests =
                    currentValue.tests +
                        TestResult(
                            name = testCase.name.testName,
                            result = TestResultStatus.Skipped(reason),
                            duration = Duration.ZERO,
                        ),
            )
        }
    }

    /**
     * Invoked when all the invocations of a [TestCase] have completed.
     * This function will only be invoked if a test case was enabled.
     */
    override suspend fun testFinished(
        testCase: TestCase,
        result: KotestTestResult,
    ) {
        val name = FullyQualifiedClassName(checkNotNull(testCase.spec.javaClass.kotlin.qualifiedName))
        this.results.compute(name) { _, currentValue ->
            checkNotNull(currentValue) {
                "testFinished event for class '$name' and test '${testCase.name.testName}' that hasn't been started."
            }

            currentValue.copy(
                tests =
                    currentValue.tests +
                        TestResult(
                            name = testCase.name.testName,
                            result = result.toTestResultStatus(),
                            duration = result.duration.toJavaDuration(),
                        ),
            )
        }
    }
}

internal fun KotestTestResult.toTestResultStatus(): TestResultStatus =
    when (this) {
        is KotestTestResult.Success -> TestResultStatus.Passed
        is KotestTestResult.Failure -> TestResultStatus.Failure(cause = this.cause)
        is KotestTestResult.Ignored -> TestResultStatus.Skipped(reason = this.reason)
        is KotestTestResult.Error -> TODO()
    }
