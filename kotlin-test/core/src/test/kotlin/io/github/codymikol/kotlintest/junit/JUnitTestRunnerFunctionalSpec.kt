package io.github.codymikol.kotlintest.junit

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintest.TestRunResult
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.assertions.json.shouldEqualSpecifiedJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class JUnitTestRunnerFunctionalSpec :
    FunSpec({
        context("functional") {
            test("disabled") {
                val result = JUnitTestRunner.run(listOf(JUnitDisabledExample::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintest.junit.JUnitDisabledExample",
                        "type": "CONTAINER",
                        "status": "IGNORED",
                        "tests": [
                          {
                            "name": "fail",
                            "duration": 0,
                            "status": {
                              "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                              "type": "IGNORED"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "pass",
                            "duration": 0,
                            "status": {
                              "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                              "type": "IGNORED"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "NestedJUnitDisabledExample",
                            "type": "CONTAINER",
                            "status": "IGNORED",
                            "tests": [
                              {
                                "name": "fail",
                                "duration": 0,
                                "status": {
                                  "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                                  "type": "IGNORED"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "pass",
                                "duration": 0,
                                "status": {
                                  "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                                  "type": "IGNORED"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "NestedNestedJUnitDisabledExample",
                                "type": "CONTAINER",
                                "status": "IGNORED",
                                "tests": [
                                  {
                                    "name": "fail",
                                    "duration": 0,
                                    "status": {
                                      "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                                      "type": "IGNORED"
                                    },
                                    "type": "TEST"
                                  },
                                  {
                                    "name": "pass",
                                    "duration": 0,
                                    "status": {
                                      "reason": "class io.github.codymikol.kotlintest.junit.JUnitDisabledExample is @Disabled",
                                      "type": "IGNORED"
                                    },
                                    "type": "TEST"
                                  }
                                ],
                                "duration": 0
                              }
                            ],
                            "duration": 0
                          }
                        ],
                        "duration": 0
                      }
                    ]
                    """.trimIndent()
            }

            test("run deeply nested test") {
                val result =
                    JUnitTestRunner.run(
                        classes = listOf(JUnitExample::class),
                        filter = "${JUnitExample::class.qualifiedName}::NestedJUnitExample::NestedNestedJUnitExample::pass",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "type": "CONTAINER",
                        "tests": [
                          {
                            "name": "NestedJUnitExample",
                            "type": "CONTAINER",
                            "tests": [
                              {
                                "name": "NestedNestedJUnitExample",
                                "type": "CONTAINER",
                                "tests": [
                                  {
                                    "name": "pass",
                                    "status": {
                                      "type": "SUCCESS"
                                    },
                                    "type": "TEST"
                                  }
                                ],
                                "status": "SUCCESS"
                              }
                            ],
                            "status": "SUCCESS"
                          }
                        ],
                        "status": "SUCCESS"
                      }
                    ]
                    """.trimIndent()
            }

            test("run nested namespace") {
                val result =
                    JUnitTestRunner.run(
                        classes = listOf(JUnitExample::class),
                        filter = "${JUnitExample::class.qualifiedName}::NestedJUnitExample::NestedNestedJUnitExample",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "type": "CONTAINER",
                        "tests": [
                          {
                            "name": "NestedJUnitExample",
                            "type": "CONTAINER",
                            "tests": [
                              {
                                "name": "NestedNestedJUnitExample",
                                "type": "CONTAINER",
                                "tests": [
                                  {
                                    "name": "fail",
                                    "status": {
                                      "error": {
                                        "message": "expected: <1> but was: <2>",
                                        "lineNumber": 151,
                                        "filename": "AssertionFailureBuilder.java"
                                      },
                                      "type": "FAILURE"
                                    },
                                    "type": "TEST"
                                  },
                                  {
                                    "name": "pass",
                                    "status": {
                                      "type": "SUCCESS"
                                    },
                                    "type": "TEST"
                                  }
                                ],
                                "status": "FAILURE"
                              }
                            ],
                            "status": "FAILURE"
                          }
                        ],
                        "status": "FAILURE"
                      }
                    ]
                    """.trimIndent()
            }

            test("run top-level namespace") {
                val result =
                    JUnitTestRunner.run(
                        classes = listOf(JUnitExample::class),
                        filter = "${JUnitExample::class.qualifiedName}::NestedJUnitExample",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "type": "CONTAINER",
                        "tests": [
                          {
                            "name": "NestedJUnitExample",
                            "type": "CONTAINER",
                            "tests": [
                              {
                                "name": "fail",
                                "status": {
                                  "error": {
                                    "message": "expected: <1> but was: <2>",
                                    "lineNumber": 151,
                                    "filename": "AssertionFailureBuilder.java"
                                  },
                                  "type": "FAILURE"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "pass",
                                "status": {
                                  "type": "SUCCESS"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "NestedNestedJUnitExample",
                                "type": "CONTAINER",
                                "tests": [
                                  {
                                    "name": "fail",
                                    "status": {
                                      "error": {
                                        "message": "expected: <1> but was: <2>",
                                        "lineNumber": 151,
                                        "filename": "AssertionFailureBuilder.java"
                                      },
                                      "type": "FAILURE"
                                    },
                                    "type": "TEST"
                                  },
                                  {
                                    "name": "pass",
                                    "status": {
                                      "type": "SUCCESS"
                                    },
                                    "type": "TEST"
                                  }
                                ],
                                "status": "FAILURE"
                              }
                            ],
                            "status": "FAILURE"
                          }
                        ],
                        "status": "FAILURE"
                      }
                    ]
                    """.trimIndent()
            }

            test("run top-level test") {
                val result =
                    JUnitTestRunner.run(
                        classes = listOf(JUnitExample::class),
                        filter = "${JUnitExample::class.qualifiedName}::pass",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "type": "CONTAINER",
                        "tests": [
                          {
                            "name": "pass",
                            "status": {
                              "type": "SUCCESS"
                            },
                            "type": "TEST"
                          }
                        ],
                        "status": "SUCCESS"
                      }
                    ]
                    """.trimIndent()
            }

            test("run all") {
                val result = JUnitTestRunner.run(listOf(JUnitExample::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson.shouldContainJsonKey("$[0].tests[0].duration")
                actualJson.shouldContainJsonKey("$[0].tests[0].status.stackTrace")

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintest.junit.JUnitExample",
                        "type": "CONTAINER",
                        "status": "FAILURE",
                        "tests": [
                          {
                            "name": "fail",
                            "status": {
                              "error": {
                                "message": "expected: <1> but was: <2>",
                                "lineNumber": 151,
                                "filename": "AssertionFailureBuilder.java"
                              },
                              "type": "FAILURE"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "pass",
                            "status": {
                              "type": "SUCCESS"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "disabled",
                            "duration": 0,
                            "status": {
                              "reason": "public final void io.github.codymikol.kotlintest.junit.JUnitExample.disabled() is @Disabled",
                              "type": "IGNORED"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "test name with spaces",
                            "status": {
                              "type": "SUCCESS"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "softAssertions",
                            "status": {
                              "error": {
                                "message": "Multiple Failures (5 failures)\n\torg.opentest4j.AssertionFailedError: expected: <1> but was: <4>\n\torg.opentest4j.AssertionFailedError: expected: <1> but was: <4>\n\torg.opentest4j.AssertionFailedError: expected: <1> but was: <4>\n\torg.opentest4j.AssertionFailedError: expected: <1> but was: <4>\n\torg.opentest4j.AssertionFailedError: expected: <1> but was: <4>",
                                "lineNumber": 80,
                                "filename": "AssertAll.java"
                              },
                              "type": "FAILURE"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "kotlin escaped string pass",
                            "status": {
                              "type": "SUCCESS"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "testSquares",
                            "type": "CONTAINER",
                            "status": "SUCCESS",
                            "tests": [
                              {
                                "name": "1^2 = 1",
                                "status": {
                                  "type": "SUCCESS"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "2^2 = 4",
                                "status": {
                                  "type": "SUCCESS"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "3^2 = 9",
                                "status": {
                                  "type": "SUCCESS"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "4^2 = 16",
                                "status": {
                                  "type": "SUCCESS"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "5^2 = 25",
                                "status": {
                                  "type": "SUCCESS"
                                },
                                "type": "TEST"
                              }
                            ]
                          },
                          {
                            "name": "NestedJUnitExample",
                            "type": "CONTAINER",
                            "status": "FAILURE",
                            "tests": [
                              {
                                "name": "fail",
                                "status": {
                                  "error": {
                                    "message": "expected: <1> but was: <2>",
                                    "lineNumber": 151,
                                    "filename": "AssertionFailureBuilder.java"
                                  },
                                  "type": "FAILURE"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "pass",
                                "status": {
                                  "type": "SUCCESS"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "NestedNestedJUnitExample",
                                "type": "CONTAINER",
                                "status": "FAILURE",
                                "tests": [
                                  {
                                    "name": "fail",
                                    "status": {
                                      "error": {
                                        "message": "expected: <1> but was: <2>",
                                        "lineNumber": 151,
                                        "filename": "AssertionFailureBuilder.java"
                                      },
                                      "type": "FAILURE"
                                    },
                                    "type": "TEST"
                                  },
                                  {
                                    "name": "pass",
                                    "status": {
                                      "type": "SUCCESS"
                                    },
                                    "type": "TEST"
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                      }
                    ]
                    """.trimIndent()
            }
        }
    })
