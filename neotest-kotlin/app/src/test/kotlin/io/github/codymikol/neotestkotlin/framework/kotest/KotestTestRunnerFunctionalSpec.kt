package io.github.codymikol.neotestkotlin.framework.kotest

import io.github.codymikol.neotestkotlin.framework.TestRunResult
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.assertions.json.shouldEqualSpecifiedJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.Json

class KotestTestRunnerFunctionalSpec :
    FunSpec({
        context("functional") {
            test("run") {
                val result = KotestTestRunner.run(listOf(KotestExample::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = Json.encodeToString(actual.report)

                actualJson.shouldContainJsonKey("$[0].nodes[0].duration")
                actualJson.shouldContainJsonKey("$[0].nodes[1].status.stackTrace")

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.neotestkotlin.framework.kotest.KotestExample",
                        "nodes": [
                          {
                            "type": "test",
                            "name": "pass",
                            "status": {
                              "status": "success"
                            }
                          },
                          {
                            "type": "test",
                            "name": "fail",
                            "status": {
                              "status": "failure",
                              "error": {
                                "message": "1 should be even",
                                "lineNumber": 18,
                                "filename": "KotestExample.kt"
                              }
                            }
                          },
                          {
                            "type": "container",
                            "name": "top level",
                            "nodes": [
                              {
                                "type": "test",
                                "name": "pass",
                                "status": {
                                  "status": "success"
                                }
                              },
                              {
                                "type": "test",
                                "name": "fail",
                                "status": {
                                  "status": "failure",
                                  "error": {
                                    "message": "1 should be even",
                                    "lineNumber": 27,
                                    "filename": "KotestExample.kt"
                                  }
                                }
                              },
                              {
                                "type": "test",
                                "name": "1 == 1",
                                "status": {
                                  "status": "success"
                                }
                              },
                              {
                                "type": "test",
                                "name": "1 == 2",
                                "status": {
                                  "status": "failure",
                                  "error": {
                                    "message": "expected:<2> but was:<1>",
                                    "lineNumber": 38,
                                    "filename": "KotestExample.kt"
                                  }
                                }
                              },
                              {
                                "type": "test",
                                "name": "1 == 3",
                                "status": {
                                  "status": "failure",
                                  "error": {
                                    "message": "expected:<3> but was:<1>",
                                    "lineNumber": 38,
                                    "filename": "KotestExample.kt"
                                  }
                                }
                              },
                              {
                                "type": "test",
                                "name": "1 == 4",
                                "status": {
                                  "status": "failure",
                                  "error": {
                                    "message": "expected:<4> but was:<1>",
                                    "lineNumber": 38,
                                    "filename": "KotestExample.kt"
                                  }
                                }
                              },
                              {
                                "type": "test",
                                "name": "assert softly",
                                "status": {
                                  "status": "failure",
                                  "error": {
                                    "message": "The following 3 assertions failed:\n1) 1 should be even\n   at io.github.codymikol.neotestkotlin.framework.kotest.KotestExample$1$3$4.invokeSuspend(KotestExample.kt:43)\n2) expected:<2> but was:<1>\n   at io.github.codymikol.neotestkotlin.framework.kotest.KotestExample$1$3$4.invokeSuspend(KotestExample.kt:44)\n3) expected:<3> but was:<1>\n   at io.github.codymikol.neotestkotlin.framework.kotest.KotestExample$1$3$4.invokeSuspend(KotestExample.kt:45)\n",
                                    "lineNumber": 95,
                                    "filename": "KotestExample.kt"
                                  }
                                }
                              },
                              {
                                "type": "container",
                                "name": "nested",
                                "nodes": [
                                  {
                                    "type": "test",
                                    "name": "pass",
                                    "status": {
                                      "status": "success"
                                    }
                                  },
                                  {
                                    "type": "test",
                                    "name": "fail",
                                    "status": {
                                      "status": "failure",
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
                                "type": "test",
                                "name": "ignored test",
                                "status": {
                                  "status": "ignored",
                                  "reason": "Disabled by xmethod"
                                }
                              },
                              {
                                "type": "container",
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
