package io.github.codymikol.kotlintestlauncher.kotest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintestlauncher.TestRunResult
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.assertions.json.shouldEqualSpecifiedJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class KotestTestRunnerFunctionalSpec :
    FunSpec({
        context("functional") {
            test("run") {
                val result = KotestTestRunner.run(listOf(KotestExample::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson.shouldContainJsonKey("$[0].tests[0].duration")
                actualJson.shouldContainJsonKey("$[0].tests[1].status.stackTrace")

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.kotlintestlauncher.kotest.KotestExample",
                        "tests": [
                          {
                            "type": "TEST",
                            "name": "pass",
                            "status": {
                              "type": "SUCCESS"
                            }
                          },
                          {
                            "type": "TEST",
                            "name": "fail",
                            "status": {
                              "type": "FAILURE",
                              "error": {
                                "message": "1 should be even",
                                "lineNumber": 18,
                                "filename": "KotestExample.kt"
                              }
                            }
                          },
                          {
                            "type": "CONTAINER",
                            "name": "top level",
                            "tests": [
                              {
                                "type": "TEST",
                                "name": "pass",
                                "status": {
                                  "type": "SUCCESS"
                                }
                              },
                              {
                                "type": "TEST",
                                "name": "fail",
                                "status": {
                                  "type": "FAILURE",
                                  "error": {
                                    "message": "1 should be even",
                                    "lineNumber": 27,
                                    "filename": "KotestExample.kt"
                                  }
                                }
                              },
                              {
                                "type": "TEST",
                                "name": "1 == 1",
                                "status": {
                                  "type": "SUCCESS"
                                }
                              },
                              {
                                "type": "TEST",
                                "name": "1 == 2",
                                "status": {
                                  "type": "FAILURE",
                                  "error": {
                                    "message": "expected:<2> but was:<1>",
                                    "lineNumber": 38,
                                    "filename": "KotestExample.kt"
                                  }
                                }
                              },
                              {
                                "type": "TEST",
                                "name": "1 == 3",
                                "status": {
                                  "type": "FAILURE",
                                  "error": {
                                    "message": "expected:<3> but was:<1>",
                                    "lineNumber": 38,
                                    "filename": "KotestExample.kt"
                                  }
                                }
                              },
                              {
                                "type": "TEST",
                                "name": "1 == 4",
                                "status": {
                                  "type": "FAILURE",
                                  "error": {
                                    "message": "expected:<4> but was:<1>",
                                    "lineNumber": 38,
                                    "filename": "KotestExample.kt"
                                  }
                                }
                              },
                              {
                                "type": "TEST",
                                "name": "assert softly",
                                "status": {
                                  "type": "FAILURE",
                                  "error": {
                                    "message": "The following 3 assertions failed:\n1) 1 should be even\n   at io.github.codymikol.kotlintestlauncher.kotest.KotestExample$1$3$4.invokeSuspend(KotestExample.kt:43)\n2) expected:<2> but was:<1>\n   at io.github.codymikol.kotlintestlauncher.kotest.KotestExample$1$3$4.invokeSuspend(KotestExample.kt:44)\n3) expected:<3> but was:<1>\n   at io.github.codymikol.kotlintestlauncher.kotest.KotestExample$1$3$4.invokeSuspend(KotestExample.kt:45)\n",
                                    "lineNumber": 95,
                                    "filename": "KotestExample.kt"
                                  }
                                }
                              },
                              {
                                "type": "CONTAINER",
                                "name": "nested",
                                "tests": [
                                  {
                                    "type": "TEST",
                                    "name": "pass",
                                    "status": {
                                      "type": "SUCCESS"
                                    }
                                  },
                                  {
                                    "type": "TEST",
                                    "name": "fail",
                                    "status": {
                                      "type": "FAILURE",
                                      "error": {
                                        "message": "1 should be even",
                                        "lineNumber": 55,
                                        "filename": "KotestExample.kt"
                                      }
                                    }
                                  }
                                ]
                              },
                              {
                                "type": "TEST",
                                "name": "ignored test",
                                "status": {
                                  "type": "IGNORED",
                                  "reason": "Disabled by xmethod"
                                }
                              },
                              {
                                "type": "CONTAINER",
                                "name": "ignored context"
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
