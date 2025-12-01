package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.execute.TestStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.test.TestResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Duration

class TestResultSpec :
    FunSpec({
        context("TestStatus") {
            context("from Kotest") {
                test("Success") {
                    val actual = TestStatus.from(TestResult.Success(Duration.ZERO))
                    actual shouldBe TestStatus.Success
                }

                test("Failure") {
                    val assertionError = AssertionError()
                    val actual = TestStatus.from(TestResult.Failure(duration = Duration.ZERO, cause = assertionError))
                    actual.shouldBeInstanceOf<TestStatus.Failure>()
                }

                test("Ignored") {
                    val actual = TestStatus.from(TestResult.Ignored(reason = "reason it's ignored"))
                    actual shouldBe TestStatus.Ignored("reason it's ignored")
                }
            }
        }
    })
