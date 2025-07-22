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
                val result = KotestTestRunner.run(listOf(KotestExampleSpec::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = Json.encodeToString(actual.report)

                actualJson.shouldContainJsonKey("$[0].nodes[0].duration")
                actualJson.shouldContainJsonKey("$[0].nodes[1].status.stackTrace")

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "name": "io.github.codymikol.neotestkotlin.framework.kotest.KotestExampleSpec",
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
                                "lineNumber": 20,
                                "filename": "KotestExampleSpec.kt"
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
                                    "lineNumber": 29,
                                    "filename": "KotestExampleSpec.kt"
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
                                    "lineNumber": 40,
                                    "filename": "KotestExampleSpec.kt"
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
                                    "lineNumber": 40,
                                    "filename": "KotestExampleSpec.kt"
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
                                    "lineNumber": 40,
                                    "filename": "KotestExampleSpec.kt"
                                  }
                                }
                              },
                              {
                                "type": "test",
                                "name": "assert softly",
                                "status": {
                                  "status": "failure",
                                  "error": {
                                    "message": "The following 3 assertions failed:\n1) 1 should be even\n   at io.github.codymikol.neotestkotlin.framework.kotest.KotestExampleSpec$1$3$4.invokeSuspend(KotestExampleSpec.kt:45)\n2) expected:<2> but was:<1>\n   at io.github.codymikol.neotestkotlin.framework.kotest.KotestExampleSpec$1$3$4.invokeSuspend(KotestExampleSpec.kt:46)\n3) expected:<3> but was:<1>\n   at io.github.codymikol.neotestkotlin.framework.kotest.KotestExampleSpec$1$3$4.invokeSuspend(KotestExampleSpec.kt:47)\n",
                                    "lineNumber": 97,
                                    "filename": "KotestExampleSpec.kt"
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
                                        "lineNumber": 57,
                                        "filename": "KotestExampleSpec.kt"
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
