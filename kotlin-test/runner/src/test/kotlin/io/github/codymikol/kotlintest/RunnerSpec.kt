package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.execute.RunnerOptions
import io.github.codymikol.kotlintest.execute.TestResult
import io.github.codymikol.kotlintest.execute.TestStatus
import io.github.codymikol.kotlintest.execute.main
import io.github.codymikol.kotlintest.execute.toJson
import io.github.codymikol.kotlintest.execute.toJsonString
import io.github.codymikol.kotlintest.kotest.KotestExample
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.json.shouldEqualSpecifiedJsonIgnoringOrder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

class RunnerSpec :
    FunSpec({
        context("RunnerOptions") {
            test("parses all options") {
                RunnerOptions.parse(
                    arrayOf(
                        "--classes=org.example.A,org.example.B",
                        "--output=/tmp/out.json",
                        "--filter=org.example.A::name with = and spaces::test",
                    ),
                ) shouldBe
                    RunnerOptions(
                        classes = listOf("org.example.A", "org.example.B"),
                        output = File("/tmp/out.json"),
                        filter = "org.example.A::name with = and spaces::test",
                    )
            }

            test("filter is optional") {
                RunnerOptions.parse(arrayOf("--classes=org.example.A", "--output=/tmp/out.json")).filter shouldBe null
            }

            test("rejects unknown options") {
                shouldThrow<IllegalArgumentException> {
                    RunnerOptions.parse(arrayOf("--classes=a", "--output=b", "--unknown=c"))
                }
            }

            test("requires classes and output") {
                shouldThrow<IllegalArgumentException> { RunnerOptions.parse(arrayOf("--output=b")) }
                shouldThrow<IllegalArgumentException> { RunnerOptions.parse(arrayOf("--classes=a")) }
            }
        }

        context("JSON") {
            test("escapes strings") {
                "a \"quoted\" \\ path\n\ttab\u0001".toJsonString() shouldBe
                    "\"a \\\"quoted\\\" \\\\ path\\n\\ttab\\u0001\""
                null.toJsonString() shouldBe "null"
            }

            test("serializes every TestStatus") {
                setOf(
                    TestResult("org.example.A", "pass", 1.milliseconds, TestStatus.Success),
                    TestResult("org.example.A", "skip", 0.milliseconds, TestStatus.Ignored("disabled")),
                    TestResult(
                        "org.example.A",
                        "namespace::fail",
                        2.milliseconds,
                        TestStatus.Failure(
                            stackTrace = "trace",
                            error = TestStatus.Failure.Error(message = null, lineNumber = 5, filename = "A.kt"),
                        ),
                    ),
                ).toJson() shouldEqualJson
                    """
                    [
                      {"className": "org.example.A", "id": "pass", "duration": 1000000, "status": {"type": "SUCCESS"}},
                      {
                        "className": "org.example.A",
                        "id": "skip",
                        "duration": 0,
                        "status": {"type": "IGNORED", "reason": "disabled"}
                      },
                      {
                        "className": "org.example.A",
                        "id": "namespace::fail",
                        "duration": 2000000,
                        "status": {
                          "type": "FAILURE",
                          "stackTrace": "trace",
                          "error": {"message": null, "lineNumber": 5, "filename": "A.kt"}
                        }
                      }
                    ]
                    """.trimIndent()
            }
        }

        test("main writes the report") {
            val output = tempfile()

            main(
                arrayOf(
                    "--classes=${KotestExample::class.qualifiedName}",
                    "--output=${output.absolutePath}",
                    "--filter=${KotestExample::class.qualifiedName}::pass",
                ),
            )

            output.readText() shouldEqualSpecifiedJsonIgnoringOrder
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
                      "type": "IGNORED"
                    }
                  }
                ]
                """.trimIndent()
        }
    })
