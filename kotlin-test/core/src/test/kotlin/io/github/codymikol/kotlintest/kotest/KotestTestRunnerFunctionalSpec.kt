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
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "fail",
                        "duration": 0,
                        "status": {
                          "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/fail is excluded by filter(s)",
                          "type": "IGNORED"
                        }
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
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "pass",
                        "duration": 0,
                        "status": {
                          "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/pass is excluded by filter(s)",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "fail",
                        "duration": 0,
                        "status": {
                          "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/fail is excluded by filter(s)",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::fail",
                        "status": {
                          "error": {
                            "message": "1 should be even",
                            "lineNumber": 25,
                            "filename": "KotestExample.kt"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::assert softly",
                        "status": {
                          "error": {
                            "message": "The following 3 assertions failed:\n1) 1 should be even\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:30)\n2) expected:<2> but was:<1>\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:31)\n3) expected:<3> but was:<1>\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:32)\n",
                            "lineNumber": 88,
                            "filename": "KotestExample.kt"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::nested::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::nested::fail",
                        "status": {
                          "error": {
                            "message": "1 should be even",
                            "lineNumber": 42,
                            "filename": "KotestExample.kt"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::ignored test",
                        "duration": 0,
                        "status": {
                          "reason": "Disabled by xmethod",
                          "type": "IGNORED"
                        }
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
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "pass",
                        "duration": 0,
                        "status": {
                          "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/pass is excluded by filter(s)",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "fail",
                        "duration": 0,
                        "status": {
                          "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/fail is excluded by filter(s)",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::pass",
                        "duration": 0,
                        "status": {
                          "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/top level -- pass is excluded by filter(s)",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::fail",
                        "duration": 0,
                        "status": {
                          "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/top level -- fail is excluded by filter(s)",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::assert softly",
                        "duration": 0,
                        "status": {
                          "reason": "io.github.codymikol.kotlintest.kotest.KotestExample/top level -- assert softly is excluded by filter(s)",
                          "type": "IGNORED"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::nested::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::nested::fail",
                        "status": {
                          "error": {
                            "message": "1 should be even",
                            "lineNumber": 42,
                            "filename": "KotestExample.kt"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::ignored test",
                        "duration": 0,
                        "status": {
                          "reason": "Disabled by xmethod",
                          "type": "IGNORED"
                        }
                      }
                    ]
                    """.trimIndent()
            }

            test("run all") {
                val result = KotestTestRunner.run(listOf(KotestExample::class))
                val actual = result.shouldBeInstanceOf<TestRunResult.Success>()
                val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(actual.report)

                actualJson.shouldContainJsonKey("$[0].duration")
                actualJson.shouldContainJsonKey("$[1].status.stackTrace")

                actualJson shouldEqualSpecifiedJson
                    """
                    [
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "fail",
                        "status": {
                          "error": {
                            "message": "1 should be even",
                            "lineNumber": 16,
                            "filename": "KotestExample.kt"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::fail",
                        "status": {
                          "error": {
                            "message": "1 should be even",
                            "lineNumber": 25,
                            "filename": "KotestExample.kt"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::assert softly",
                        "status": {
                          "error": {
                            "message": "The following 3 assertions failed:\n1) 1 should be even\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:30)\n2) expected:<2> but was:<1>\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:31)\n3) expected:<3> but was:<1>\n   at io.github.codymikol.kotlintest.kotest.KotestExample$1$3$3.invokeSuspend(KotestExample.kt:32)\n",
                            "lineNumber": 88,
                            "filename": "KotestExample.kt"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::nested::pass",
                        "status": {
                          "type": "SUCCESS"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::nested::fail",
                        "status": {
                          "error": {
                            "message": "1 should be even",
                            "lineNumber": 42,
                            "filename": "KotestExample.kt"
                          },
                          "type": "FAILURE"
                        }
                      },
                      {
                        "className": "io.github.codymikol.kotlintest.kotest.KotestExample",
                        "id": "top level::ignored test",
                        "duration": 0,
                        "status": {
                          "reason": "Disabled by xmethod",
                          "type": "IGNORED"
                        }
                      }
                    ]
                    """.trimIndent()
            }
        }
    })
