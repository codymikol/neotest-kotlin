package io.github.codymikol.kotlintest

import org.junit.platform.engine.TestExecutionResult
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import io.kotest.engine.test.TestResult as KotestTestResult

/**
 * A result for a test execution. Serialized as JSON.
 *
 * Success:
 *
 * ```json
 * {
 *   "className": "org.example.TestEaxmple",
 *   "id": "namespace::nested namespace::test",
 *   "duration": 1234567,
 *   "status": {
 *     "type": "SUCCESS"
 *   }
 * }
 * ```
 *
 * Ignored:
 *
 * ```json
 * {
 *   "className": "org.example.TestEaxmple",
 *   "id": "namespace::nested namespace::test",
 *   "duration": 1234567,
 *   "status": {
 *     "type": "IGNORED",
 *     "reason": "disabled by xmethod"
 *   }
 * }
 * ```
 *
 * Failure:
 *
 * ```json
 * {
 *   "className": "org.example.TestEaxmple",
 *   "id": "namespace::nested namespace::test",
 *   "duration": 1234567,
 *   "status": {
 *     "type": "FAILURE",
 *     "error": {
 *       "message": "exception message",
 *       "lineNumber: 5,
 *       "filename": "/this/is/a/path/to/org/example/TestExample.kt"
 *     }
 *   }
 * }
 * ```
 */
public data class TestResult(
    /**
     * The fully qualified top level class name of this [TestResult].
     */
    val className: String,
    /**
     * '::' separated identifier of this [TestResult].
     *
     * ```
     * namespace::nested namespace::test
     * namespace::test
     * test
     * ```
     */
    val id: String,
    /**
     * The execution time of this test.
     */
    val duration: Duration,
    /**
     * The status of this [TestResult].
     */
    val status: TestStatus,
)

public enum class Status {
    SUCCESS,
    FAILURE,
    IGNORED,
}

public sealed interface TestStatus {
    /**
     * The type of [TestStatus] as defined by [Status].
     *
     * - SUCCESS
     * - FAILURE
     * - IGNORED
     */
    public val type: Status

    public companion object {
        internal fun from(result: TestExecutionResult): TestStatus =
            when (result.status) {
                TestExecutionResult.Status.SUCCESSFUL -> Success
                TestExecutionResult.Status.FAILED -> {
                    val error = result.throwable.getOrNull()

                    Failure(
                        stackTrace = error?.stackTraceToString(),
                        error =
                        error?.run {
                            val traceOrigin = stackTrace?.firstOrNull()

                            Failure.Error(
                                message = message,
                                lineNumber = traceOrigin?.lineNumber,
                                filename = traceOrigin?.fileName,
                            )
                        },
                    )
                }
                TestExecutionResult.Status.ABORTED -> TODO()
            }

        internal fun from(kotestResult: KotestTestResult): TestStatus =
            when (kotestResult) {
                is KotestTestResult.Success -> Success
                is KotestTestResult.Failure -> {
                    val error = kotestResult.errorOrNull

                    Failure(
                        stackTrace = error?.stackTraceToString(),
                        error =
                        error?.run {
                            val traceOrigin = stackTrace?.firstOrNull()

                            Failure.Error(
                                message = message,
                                lineNumber = traceOrigin?.lineNumber,
                                filename = traceOrigin?.fileName,
                            )
                        },
                    )
                }
                is KotestTestResult.Ignored -> Ignored(reason = kotestResult.reason)
                is KotestTestResult.Error -> TODO()
            }
    }

    public object Success : TestStatus {
        override val type: Status = Status.SUCCESS
    }

    public data class Failure(
        /**
         * The entire stacktrace as a String, this is useful for displaying what
         * a thrown exception would yield in the console.
         */
        public val stackTrace: String?,
        /**
         * Programmatic view of the original error.
         */
        public val error: Error?,
    ) : TestStatus {
        override val type: Status = Status.FAILURE

        public data class Error(
            /**
             * The message of the Exception.
             */
            public val message: String?,
            /**
             * The line number which threw the exception in the [filename].
             */
            public val lineNumber: Int?,
            /**
             * The file in which the exception originates. This
             * is your test file.
             */
            public val filename: String?,
        )
    }

    public data class Ignored(
        /**
         * The reason the test was ignored.
         */
        public val reason: String? = null,
    ) : TestStatus {
        override val type: Status = Status.IGNORED
    }
}
