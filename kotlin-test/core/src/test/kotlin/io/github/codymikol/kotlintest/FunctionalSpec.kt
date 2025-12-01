package io.github.codymikol.kotlintest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintest.execute.TestFrameworkRunner
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.core.spec.style.FunSpec

class FunctionalSpec :
    FunSpec({
        test("functional") {
            val result = TestFrameworkRunner.runAll(classes = setOf(AllFrameworksInOneExample::class))
            val actualJson = ObjectMapper().registerKotlinModule().writeValueAsString(result)

            actualJson shouldEqualSpecifiedJsonIgnoringOrder
                """
                [
                  {
                    "className": "io.github.codymikol.kotlintest.AllFrameworksInOneExample",
                    "id": "namespace::pass",
                    "status": {
                      "type": "SUCCESS"
                    }
                  },
                  {
                    "className": "io.github.codymikol.kotlintest.AllFrameworksInOneExample",
                    "id": "namespace::fail",
                    "status": {
                      "error": {
                        "message": "2 should be odd",
                        "lineNumber": 18,
                        "filename": "AllFrameworksInOneExample.kt"
                      },
                      "type": "FAILURE"
                    }
                  },
                  {
                    "className": "io.github.codymikol.kotlintest.AllFrameworksInOneExample",
                    "id": "namespace::pass",
                    "status": {
                      "type": "SUCCESS"
                    }
                  },
                  {
                    "className": "io.github.codymikol.kotlintest.AllFrameworksInOneExample",
                    "id": "namespace::fail",
                    "status": {
                      "error": {
                        "message": "2 should be odd",
                        "lineNumber": 18,
                        "filename": "AllFrameworksInOneExample.kt"
                      },
                      "type": "FAILURE"
                    }
                  },
                  {
                    "className": "io.github.codymikol.kotlintest.AllFrameworksInOneExample",
                    "id": "kotlinFail",
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
                    "className": "io.github.codymikol.kotlintest.AllFrameworksInOneExample",
                    "id": "kotlinPass",
                    "status": {
                      "type": "SUCCESS"
                    }
                  },
                  {
                    "className": "io.github.codymikol.kotlintest.AllFrameworksInOneExample",
                    "id": "junitFail",
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
                    "className": "io.github.codymikol.kotlintest.AllFrameworksInOneExample",
                    "id": "junitPass",
                    "status": {
                      "type": "SUCCESS"
                    }
                  },
                  {
                    "className": "io.github.codymikol.kotlintest.AllFrameworksInOneExample",
                    "id": "JUnitNamespace::junitFail",
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
                    "className": "io.github.codymikol.kotlintest.AllFrameworksInOneExample",
                    "id": "JUnitNamespace::junitPass",
                    "status": {
                      "type": "SUCCESS"
                    }
                  }
                ]
                """.trimIndent()
        }
    })
