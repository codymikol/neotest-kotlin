package io.github.codymikol.kotlintest.junit

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintest.execute.TestRunResult
import io.github.codymikol.kotlintest.execute.junit.JUnitTestExecutor
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class JUnitTestExecutorFunctionalSpec :
    FunSpec({
        context("functional") {
            test("disabled") {
                val result = JUnitTestExecutor.run(listOf(JUnitDisabledExample::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJsonIgnoringOrder
                    """
                    [
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitDisabledExample",
                        "id": "fail",
                        "duration": 0,
                        "status": {
                          "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitDisabledExample",
                        "id": "pass",
                        "duration": 0,
                        "status": {
                          "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitDisabledExample",
                        "id": "NestedJUnitDisabledExample::fail",
                        "duration": 0,
                        "status": {
                          "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitDisabledExample",
                        "id": "NestedJUnitDisabledExample::pass",
                        "duration": 0,
                        "status": {
                          "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitDisabledExample",
                        "id": "NestedJUnitDisabledExample::NestedNestedJUnitDisabledExample::fail",
                        "duration": 0,
                        "status": {
                          "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitDisabledExample",
                        "id": "NestedJUnitDisabledExample::NestedNestedJUnitDisabledExample::pass",
                        "duration": 0,
                        "status": {
                          "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                          "type": "IGNORED"
                        }
                      }
                    ]
                    """.trimIndent()
            }

            test("run deeply nested test") {
                val result =
                    JUnitTestExecutor.run(
                        classes = listOf(JUnitExample::class),
                        filter =
                        "${JUnitExample::class.qualifiedName}::NestedJUnitExample::NestedNestedJUnitExample::pass",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJsonIgnoringOrder
                    """
                    [
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::NestedNestedJUnitExample::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      }
                    ]
                    """.trimIndent()
            }

            test("run nested namespace") {
                val result =
                    JUnitTestExecutor.run(
                        classes = listOf(JUnitExample::class),
                        filter = "${JUnitExample::class.qualifiedName}::NestedJUnitExample::NestedNestedJUnitExample",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJsonIgnoringOrder
                    """
                    [
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::NestedNestedJUnitExample::fail",
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
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::NestedNestedJUnitExample::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      }
                    ]
                    """.trimIndent()
            }

            test("run top-level namespace") {
                val result =
                    JUnitTestExecutor.run(
                        classes = listOf(JUnitExample::class),
                        filter = "${JUnitExample::class.qualifiedName}::NestedJUnitExample",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJsonIgnoringOrder
                    """
                    [
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::fail",
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
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::NestedNestedJUnitExample::fail",
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
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::NestedNestedJUnitExample::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      }
                    ]
                    """.trimIndent()
            }

            test("run top-level test") {
                val result =
                    JUnitTestExecutor.run(
                        classes = listOf(JUnitExample::class),
                        filter = "${JUnitExample::class.qualifiedName}::pass",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJsonIgnoringOrder
                    """
                    [
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      }
                    ]
                    """.trimIndent()
            }

            test("run all") {
                val result = JUnitTestExecutor.run(listOf(JUnitExample::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson.shouldContainJsonKey("$[0].duration")
                actualJson.shouldContainJsonKey("$[0].status.stackTrace")

                actualJson shouldEqualSpecifiedJsonIgnoringOrder
                    """
                    [
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
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
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "disabled",
                        "duration": 0,
                        "status": {
                          "reason": "public final void io.github.codymikol.kotlintest.junit.JUnitExample.disabled() is @Disabled",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "test name with spaces",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "softAssertions",
                        "status": {
                          "error": {
                            "message": "Multiple Failures (5 failures)\n\torg.opentest4j.AssertionFailedError: expected: <1> but was: <4>\n\torg.opentest4j.AssertionFailedError: expected: <1> but was: <4>\n\torg.opentest4j.AssertionFailedError: expected: <1> but was: <4>\n\torg.opentest4j.AssertionFailedError: expected: <1> but was: <4>\n\torg.opentest4j.AssertionFailedError: expected: <1> but was: <4>",
                            "lineNumber": 80,
                            "filename": "AssertAll.java"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "kotlin escaped string pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "testSquares::1^2 = 1",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "testSquares::2^2 = 4",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "testSquares::3^2 = 9",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "testSquares::4^2 = 16",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "testSquares::5^2 = 26",
                        "status": {
                          "error": {
                            "message": "expected: <26> but was: <25>",
                            "lineNumber": 151,
                            "filename": "AssertionFailureBuilder.java"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "testSquares",
                        "status": {
                          "error": {
                            "message": "expected: <26> but was: <25>",
                            "lineNumber": 151,
                            "filename": "AssertionFailureBuilder.java"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::fail",
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
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::NestedNestedJUnitExample::fail",
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
                        "className": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "id": "NestedJUnitExample::NestedNestedJUnitExample::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      }
                    ]
                    """.trimIndent()
            }
        }
    })
