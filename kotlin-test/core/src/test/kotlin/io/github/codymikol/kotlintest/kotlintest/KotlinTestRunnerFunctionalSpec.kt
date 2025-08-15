package io.github.codymikol.kotlintest.kotlintest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintest.TestRunResult
import io.github.codymikol.kotlintest.junit.JUnitTestRunner
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class KotlinTestRunnerFunctionalSpec :
    FunSpec({
        context("functional") {
            test("run") {
                val result = JUnitTestRunner.run(listOf(KotlinTestExample::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

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
