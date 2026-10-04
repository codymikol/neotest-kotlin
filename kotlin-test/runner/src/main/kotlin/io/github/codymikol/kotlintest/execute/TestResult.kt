package io.github.codymikol.kotlintest.execute

import kotlin.time.Duration

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
 *
 * Class level failure, e.g. an exception in `beforeSpec`/`@BeforeAll`, a class that cannot be
 * instantiated or a test engine error. These have an empty [id] and apply to the whole class:
 *
 * ```json
 * {
 *   "className": "org.example.TestEaxmple",
 *   "id": "",
 *   "duration": 0,
 *   "status": {
 *     "type": "FAILURE",
 *     "error": {
 *       "message": "exception message",
 *       "lineNumber: null,
 *       "filename": null
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
     *
     * Empty when this result applies to the entire class rather than a single test.
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

        internal companion object {
            /**
             * Creates a [Failure] for an error thrown by a test of any test framework, the error
             * location being the origin of the [error].
             */
            internal fun from(error: Throwable?): Failure =
                Failure(
                    stackTrace = error?.stackTraceToString(),
                    error =
                    error?.run {
                        val traceOrigin = stackTrace?.firstOrNull()

                        Error(
                            message = message,
                            lineNumber = traceOrigin?.lineNumber,
                            filename = traceOrigin?.fileName,
                        )
                    },
                )

            /**
             * Creates a [Failure] for an error that failed an entire class (spec) rather than a single test,
             * e.g. an exception in a `beforeSpec` callback or a constructor that throws.
             *
             * The error location is the first stack frame belonging to [className] (in the
             * entire cause chain) as frames of the test framework aren't useful to the user.
             */
            internal fun fromClassError(
                className: String,
                error: Throwable,
            ): Failure {
                val causes = generateSequence(error) { it.cause.takeIf { cause -> cause !== it } }.toList()
                val rootCause = causes.last()
                val traceOrigin =
                    causes
                        .flatMap { it.stackTrace.orEmpty().asIterable() }
                        .firstOrNull { it.className == className || it.className.startsWith("$className$") }

                // wrapping exceptions (e.g. "Could not create instance of class") hide the actual reason
                val message =
                    when {
                        rootCause === error -> error.message ?: error.toString()
                        error.message?.contains(rootCause.toString()) == true -> error.message
                        else -> listOfNotNull(error.message, rootCause.toString()).joinToString(separator = ": ")
                    }

                return Failure(
                    stackTrace = error.stackTraceToString(),
                    error =
                    Error(
                        message = message,
                        lineNumber = traceOrigin?.lineNumber,
                        filename = traceOrigin?.fileName,
                    ),
                )
            }

            /**
             * Creates a [Failure] for errors of the test engine itself, which aren't attributable
             * to any line of a test file.
             */
            internal fun fromEngineErrors(errors: List<Throwable>): Failure =
                Failure(
                    stackTrace = errors.joinToString(separator = "\n") { it.stackTraceToString() },
                    error =
                    Error(
                        message =
                        errors.joinToString(separator = "\n", prefix = "Test engine failed: ") {
                            it.message ?: it.toString()
                        },
                        lineNumber = null,
                        filename = null,
                    ),
                )
        }

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
