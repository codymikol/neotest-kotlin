package io.github.codymikol.kotlintest.kotlintest

import io.github.codymikol.kotlintest.execute.TestRunResult
import io.github.codymikol.kotlintest.execute.junit.JUnitTestExecutor
import io.github.codymikol.kotlintest.execute.toJson
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class KotlinTestRunnerFunctionalSpec :
    FunSpec({
        context("functional") {
            test("run") {
                val result = JUnitTestExecutor.run(listOf(KotlinTestExample::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = actual.report.toJson()

                actualJson.shouldContainJsonKey("$[0].duration")
                actualJson.shouldContainJsonKey("$[0].status.stackTrace")

                actualJson shouldEqualSpecifiedJsonIgnoringOrder
                    """
                    [
                      {
                        "className": "io.github.codymikol.kotlintest.kotlintest.KotlinTestExample",
                        "id": "fail",
                        "status": {
                          "error": {
                            "message": "expected: <1> but was: <2>",
                            "lineNumber": 151,
                            "filename": "AssertionFailureBuilder.java"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotlintest.KotlinTestExample",
                        "id": "pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      }
                    ]
                    """.trimIndent()
            }
        }
    })
