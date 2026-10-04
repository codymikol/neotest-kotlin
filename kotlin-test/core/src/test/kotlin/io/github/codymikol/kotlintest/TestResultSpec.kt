package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.execute.TestStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.test.TestResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.platform.engine.TestExecutionResult
import org.opentest4j.TestAbortedException
import kotlin.time.Duration

class TestResultSpec :
    FunSpec({
        context("TestStatus") {
            context("from Kotest") {
                test("Success") {
                    val actual = TestStatus.from(TestResult.Success(Duration.ZERO))
                    actual shouldBe TestStatus.Success
                }

                test("Failure") {
                    val assertionError = AssertionError()
                    val actual = TestStatus.from(TestResult.Failure(duration = Duration.ZERO, cause = assertionError))
                    actual.shouldBeInstanceOf<TestStatus.Failure>()
                }

                test("Ignored") {
                    val actual = TestStatus.from(TestResult.Ignored(reason = "reason it's ignored"))
                    actual shouldBe TestStatus.Ignored("reason it's ignored")
                }

                test("Error") {
                    val error = IllegalStateException("error outside of an assertion")
                    val actual = TestStatus.from(TestResult.Error(duration = Duration.ZERO, cause = error))
                    val failure = actual.shouldBeInstanceOf<TestStatus.Failure>()

                    failure.stackTrace shouldBe error.stackTraceToString()
                    failure.error shouldBe
                        TestStatus.Failure.Error(
                            message = "error outside of an assertion",
                            lineNumber = error.stackTrace.first().lineNumber,
                            filename = "TestResultSpec.kt",
                        )
                }
            }

            context("from JUnit") {
                test("Successful") {
                    TestStatus.from(TestExecutionResult.successful()) shouldBe TestStatus.Success
                }

                test("Failed") {
                    val actual = TestStatus.from(TestExecutionResult.failed(AssertionError("failed")))
                    actual.shouldBeInstanceOf<TestStatus.Failure>().error?.message shouldBe "failed"
                }

                test("Aborted") {
                    val actual = TestStatus.from(TestExecutionResult.aborted(TestAbortedException("assumption failed")))
                    actual shouldBe TestStatus.Ignored("assumption failed")
                }

                test("Aborted without throwable") {
                    TestStatus.from(TestExecutionResult.aborted(null)) shouldBe TestStatus.Ignored(null)
                }
            }

            context("class errors") {
                test("location is the first frame of the class") {
                    val cause = IllegalStateException("root cause")
                    cause.stackTrace =
                        arrayOf(
                            StackTraceElement("kotlin.PreconditionsKt", "error", "Preconditions.kt", 1),
                            StackTraceElement("org.example.MySpec$1", "invoke", "MySpec.kt", 12),
                        )
                    val error = RuntimeException("Could not create instance of class org.example.MySpec", cause)

                    TestStatus.Failure.fromClassError("org.example.MySpec", error).error shouldBe
                        TestStatus.Failure.Error(
                            message =
                            "Could not create instance of class org.example.MySpec: " +
                                "java.lang.IllegalStateException: root cause",
                            lineNumber = 12,
                            filename = "MySpec.kt",
                        )
                }

                test("no location when the class is not part of the stack trace") {
                    val error = IllegalStateException("failed")

                    TestStatus.Failure.fromClassError("org.example.MySpec", error).error shouldBe
                        TestStatus.Failure.Error(message = "failed", lineNumber = null, filename = null)
                }
            }
        }
    })
