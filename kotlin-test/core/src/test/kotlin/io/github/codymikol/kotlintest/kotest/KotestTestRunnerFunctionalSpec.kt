package io.github.codymikol.kotlintest.kotest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintest.TestRunResult
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.assertions.json.shouldEqualSpecifiedJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class KotestTestRunnerFunctionalSpec :
    FunSpec({
        context("functional") {
            test("run top-level test") {
                val result =
                    KotestTestRunner.run(
                        classes = listOf(KotestExample::class),
                        filter = "${KotestExample::class.qualifiedName}::pass",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "type": "CONTAINER",
                        "tests": [
                          {
                            "name": "pass",
                            "status": {
                              "type": "SUCCESS"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "fail",
                            "duration": 0,
                            "status": {
                              "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/fail is excluded by filter(s)",
                              "type": "IGNORED"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "top level",
                            "type": "CONTAINER",
                            "tests": [],
                            "duration": 0,
                            "status": "IGNORED"
                          }
                        ],
                        "status": "SUCCESS"
                      }
                    ]
                    """.trimIndent()
            }

            test("run top-level namespace") {
                val result =
                    KotestTestRunner.run(
                        classes = listOf(KotestExample::class),
                        filter = "io.github.codymikol.kotlintest.kotest.KotestExample::top level",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "type": "CONTAINER",
                        "status": "FAILURE",
                        "tests": [
                          {
                            "name": "pass",
                            "status": {
                              "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/pass is excluded by filter(s)",
                              "type": "IGNORED"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "fail",
                            "status": {
                              "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/fail is excluded by filter(s)",
                              "type": "IGNORED"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "top level",
                            "type": "CONTAINER",
                            "status": "FAILURE",
                            "tests": [
                              {
                                "name": "pass",
                                "status": {
                                  "type": "SUCCESS"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "fail",
                                "status": {
                                  "error": {
                                    "message": "1 should be even",
                                    "lineNumber": 25,
                                    "filename": "KotestExample.kt"
                                  },
                                  "type": "FAILURE"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "assert softly",
                                "status": {
                                  "error": {
                                    "message": "The following 3 assertions failed:\n1) 1 should be even\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:30)\n2) expected:<2> but was:<1>\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:31)\n3) expected:<3> but was:<1>\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:32)\n",
                                    "lineNumber": 88,
                                    "filename": "KotestExample.kt"
                                  },
                                  "type": "FAILURE"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "nested",
                                "type": "CONTAINER",
                                "status": "FAILURE",
                                "tests": [
                                  {
                                    "name": "pass",
                                    "status": {
                                      "type": "SUCCESS"
                                    },
                                    "type": "TEST"
                                  },
                                  {
                                    "name": "fail",
                                    "status": {
                                      "error": {
                                        "message": "1 should be even",
                                        "lineNumber": 42,
                                        "filename": "KotestExample.kt"
                                      },
                                      "type": "FAILURE"
                                    },
                                    "type": "TEST"
                                  }
                                ]
                              },
                              {
                                "name": "ignored test",
                                "duration": 0,
                                "status": {
                                  "reason": "Disabled by xmethod",
                                  "type": "IGNORED"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "ignored context",
                                "type": "CONTAINER",
                                "status": "IGNORED",
                                "tests": [],
                                "duration": 0
                              }
                            ]
                          }
                        ]
                      }
                    ]
                    """.trimIndent()
            }

            test("run nested namespace") {
                val result =
                    KotestTestRunner.run(
                        classes = listOf(KotestExample::class),
                        filter = "io.github.codymikol.kotlintest.kotest.KotestExample::top level::nested",
                    )
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "type": "CONTAINER",
                        "status": "FAILURE",
                        "tests": [
                          {
                            "name": "pass",
                            "status": {
                              "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/pass is excluded by filter(s)",
                              "type": "IGNORED"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "fail",
                            "status": {
                              "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/fail is excluded by filter(s)",
                              "type": "IGNORED"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "top level",
                            "type": "CONTAINER",
                            "status": "FAILURE",
                            "tests": [
                              {
                                "name": "pass",
                                "status": {
                                  "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/top level -- pass is excluded by filter(s)",
                                  "type": "IGNORED"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "fail",
                                "status": {
                                  "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/top level -- fail is excluded by filter(s)",
                                  "type": "IGNORED"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "assert softly",
                                "status": {
                                  "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/top level -- assert softly is excluded by filter(s)",
                                  "type": "IGNORED"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "nested",
                                "type": "CONTAINER",
                                "status": "FAILURE",
                                "tests": [
                                  {
                                    "name": "pass",
                                    "status": {
                                      "type": "SUCCESS"
                                    },
                                    "type": "TEST"
                                  },
                                  {
                                    "name": "fail",
                                    "status": {
                                      "error": {
                                        "message": "1 should be even",
                                        "lineNumber": 42,
                                        "filename": "KotestExample.kt"
                                      },
                                      "type": "FAILURE"
                                    },
                                    "type": "TEST"
                                  }
                                ]
                              },
                              {
                                "name": "ignored test",
                                "duration": 0,
                                "status": {
                                  "reason": "Disabled by xmethod",
                                  "type": "IGNORED"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "ignored context",
                                "type": "CONTAINER",
                                "status": "IGNORED",
                                "tests": [],
                                "duration": 0
                              }
                            ]
                          }
                        ]
                      }
                    ]
                    """.trimIndent()
            }

            test("run all") {
                val result = KotestTestRunner.run(listOf(KotestExample::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson.shouldContainJsonKey("$[0].tests[0].duration")
                actualJson.shouldContainJsonKey("$[0].tests[1].status.stackTrace")

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "type": "CONTAINER",
                        "status": "FAILURE",
                        "tests": [
                          {
                            "name": "pass",
                            "status": {
                              "type": "SUCCESS"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "fail",
                            "status": {
                              "error": {
                                "message": "1 should be even",
                                "lineNumber": 16,
                                "filename": "KotestExample.kt"
                              },
                              "type": "FAILURE"
                            },
                            "type": "TEST"
                          },
                          {
                            "name": "top level",
                            "type": "CONTAINER",
                            "status": "FAILURE",
                            "tests": [
                              {
                                "name": "pass",
                                "status": {
                                  "type": "SUCCESS"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "fail",
                                "status": {
                                  "error": {
                                    "message": "1 should be even",
                                    "lineNumber": 25,
                                    "filename": "KotestExample.kt"
                                  },
                                  "type": "FAILURE"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "assert softly",
                                "status": {
                                  "error": {
                                    "message": "The following 3 assertions failed:\n1) 1 should be even\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:30)\n2) expected:<2> but was:<1>\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:31)\n3) expected:<3> but was:<1>\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:32)\n",
                                    "lineNumber": 88,
                                    "filename": "KotestExample.kt"
                                  },
                                  "type": "FAILURE"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "nested",
                                "type": "CONTAINER",
                                "status": "FAILURE",
                                "tests": [
                                  {
                                    "name": "pass",
                                    "status": {
                                      "type": "SUCCESS"
                                    },
                                    "type": "TEST"
                                  },
                                  {
                                    "name": "fail",
                                    "status": {
                                      "error": {
                                        "message": "1 should be even",
                                        "lineNumber": 42,
                                        "filename": "KotestExample.kt"
                                      },
                                      "type": "FAILURE"
                                    },
                                    "type": "TEST"
                                  }
                                ]
                              },
                              {
                                "name": "ignored test",
                                "duration": 0,
                                "status": {
                                  "reason": "Disabled by xmethod",
                                  "type": "IGNORED"
                                },
                                "type": "TEST"
                              },
                              {
                                "name": "ignored context",
                                "type": "CONTAINER",
                                "status": "IGNORED",
                                "tests": [],
                                "duration": 0
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
