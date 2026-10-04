package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.execute.TestFrameworkExecutor
import io.github.codymikol.kotlintest.execute.TestResult
import io.github.codymikol.kotlintest.execute.TestRunResult
import io.github.codymikol.kotlintest.execute.TestStatus
import io.github.codymikol.kotlintest.execute.kotest.KotestTestExecutor
import io.github.codymikol.kotlintest.execute.toJson
import io.github.codymikol.kotlintest.kotest.KotestExample
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.core.listeners.BeforeProjectListener
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.KClass
import kotlin.time.Duration

/**
 * Kotest executor whose engine fails before any spec is executed.
 */
private object FailingEngineKotestExecutor : TestFrameworkExecutor by KotestTestExecutor {
    override fun run(
        classes: Collection<KClass<*>>,
        filter: String?,
    ): TestRunResult =
        KotestTestExecutor.run(
            classes = classes,
            filter = filter,
            extensions =
            listOf(
                object : BeforeProjectListener {
                    override suspend fun beforeProject(): Unit = error("engine failed")
                },
            ),
        )
}

private class StubExecutor(
    private val result: TestRunResult,
) : TestFrameworkExecutor {
    override fun run(
        classes: Collection<KClass<*>>,
        filter: String?,
    ): TestRunResult = result

    override fun isRunnable(kclass: KClass<*>): Boolean = true
}

class TestFrameworkExecutorSpec :
    FunSpec({
        context("runAll") {
            test("engine failure marks each requested class as failed") {
                val report =
                    TestFrameworkExecutor.runAll(
                        classes = setOf(KotestExample::class),
                        filter = null,
                        executors = listOf(FailingEngineKotestExecutor),
                    )
                val actualJson = report.toJson()

                actualJson.shouldContainJsonKey("$[0].status.stackTrace")

                actualJson shouldEqualSpecifiedJsonIgnoringOrder
                    """
                    [
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "",
                        "duration": 0,
                        "status": {
                          "error": {
                            "message": "Test engine failed: java.lang.IllegalStateException: engine failed",
                            "lineNumber": null,
                            "filename": null
                          },
                          "type": "FAILURE"
                        }
                      }
                    ]
                    """.trimIndent()
            }

            test("engine failure keeps partial results and existing class level results") {
                val passed =
                    TestResult(
                        className = "org.example.A",
                        id = "pass",
                        duration = Duration.ZERO,
                        status = TestStatus.Success,
                    )
                val classFailure =
                    TestResult(
                        className = "org.example.B",
                        id = "",
                        duration = Duration.ZERO,
                        status = TestStatus.Failure(stackTrace = null, error = null),
                    )

                val report =
                    TestFrameworkExecutor.runAll(
                        classes = setOf(String::class, Int::class),
                        filter = null,
                        executors =
                        listOf(
                            StubExecutor(
                                TestRunResult.Failure(
                                    errors = listOf(IllegalStateException("engine failed")),
                                    report = setOf(passed, classFailure.copy(className = "kotlin.String")),
                                ),
                            ),
                        ),
                    )

                report.filter { it.className == "kotlin.String" } shouldBe
                    listOf(classFailure.copy(className = "kotlin.String"))
                val engineFailure = report.single { it.className == "kotlin.Int" }
                engineFailure.id shouldBe ""
                engineFailure.status.shouldBeInstanceOf<TestStatus.Failure>().error shouldBe
                    TestStatus.Failure.Error(
                        message = "Test engine failed: engine failed",
                        lineNumber = null,
                        filename = null,
                    )
                report.contains(passed) shouldBe true
            }
        }
    })
