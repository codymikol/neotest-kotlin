package io.github.codymikol.kotlintestlauncher

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestResult
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TestNodeSpec :
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

        context("TestNode") {
            context("TestNode.Container") {
                context("status") {
                    test("all passed") {
                        val container = TestNode.Container(name = "example")
                        container.add(TestNode.Test(name = "pass", duration = 5.seconds, status = TestStatus.Success))
                        container.add(TestNode.Test(name = "pass1", duration = 5.seconds, status = TestStatus.Success))
                        container.add(TestNode.Test(name = "pass2", duration = 5.seconds, status = TestStatus.Success))
                        container.add(TestNode.Test(name = "pass3", duration = 5.seconds, status = TestStatus.Success))

                        container.status shouldBe Status.SUCCESS
                    }

                    test("single failure") {
                        val container = TestNode.Container(name = "example")
                        container.add(TestNode.Test(name = "pass", duration = 5.seconds, status = TestStatus.Success))
                        container.add(
                            TestNode.Test(
                                name = "pass1",
                                duration = 5.seconds,
                                status =
                                    TestStatus.Failure(
                                        stackTrace = "",
                                        error = TestStatus.Failure.Error(message = null, lineNumber = null, filename = null),
                                    ),
                            ),
                        )
                        container.add(TestNode.Test(name = "pass2", duration = 5.seconds, status = TestStatus.Success))
                        container.add(TestNode.Test(name = "pass3", duration = 5.seconds, status = TestStatus.Success))

                        container.status shouldBe Status.FAILURE
                    }

                    test("mixture") {
                        val container = TestNode.Container(name = "example")
                        container.add(TestNode.Test(name = "pass", duration = 5.seconds, status = TestStatus.Success))
                        container.add(
                            TestNode.Test(
                                name = "failure",
                                duration = 5.seconds,
                                status =

                                    TestStatus.Failure(
                                        stackTrace = "",
                                        error = TestStatus.Failure.Error(message = null, lineNumber = null, filename = null),
                                    ),
                            ),
                        )
                        container.add(
                            TestNode.Test(
                                name = "failure1",
                                duration = 5.seconds,
                                status =
                                    TestStatus.Failure(
                                        stackTrace = "",
                                        error = TestStatus.Failure.Error(message = null, lineNumber = null, filename = null),
                                    ),
                            ),
                        )
                        container.add(TestNode.Test(name = "pass1", duration = 5.seconds, status = TestStatus.Ignored()))

                        container.status shouldBe Status.FAILURE
                    }

                    test("all ignored") {
                        val container = TestNode.Container(name = "example")
                        container.add(TestNode.Test(name = "ignored", duration = 5.seconds, status = TestStatus.Ignored()))
                        container.add(TestNode.Test(name = "ignored1", duration = 5.seconds, status = TestStatus.Ignored()))
                        container.add(TestNode.Test(name = "ignored2", duration = 5.seconds, status = TestStatus.Ignored()))
                        container.add(TestNode.Test(name = "ignored3", duration = 5.seconds, status = TestStatus.Ignored()))

                        container.status shouldBe Status.IGNORED
                    }
                }

                context("add") {
                    test("success") {
                        val container = TestNode.Container(name = "example")

                        shouldNotThrowAny {
                            container.add(TestNode.Container(name = "inner")) shouldBe true
                            container.add(TestNode.Container(name = "nested-inner"), listOf("inner")) shouldBe true
                            container.add(TestNode.Container(name = "pass"), listOf("inner", "nested-inner")) shouldBe true
                        }

                        val nodes = container.visitAllNodes().associateBy { it.name }
                        nodes shouldContainKey "inner"
                        nodes shouldContainKey "nested-inner"
                        nodes shouldContainKey "pass"
                    }

                    test("add to test that already exists") {
                        val container = TestNode.Container(name = "example")
                        container.add(TestNode.Container(name = "inner")) shouldBe true
                        container.add(TestNode.Container(name = "nested-inner"), listOf("inner")) shouldBe true
                        container.add(TestNode.Container(name = "pass"), listOf("inner", "nested-inner")) shouldBe true

                        val exception =
                            shouldThrowExactly<IllegalArgumentException> {
                                container.add(TestNode.Container(name = "pass"), listOf("inner", "nested-inner")) shouldBe true
                            }

                        exception.message shouldBe "example > inner > nested-inner > pass already exists"
                    }

                    test("add to unknown sub container") {
                        val container = TestNode.Container(name = "example")

                        val exception =
                            shouldThrowExactly<IllegalArgumentException> {
                                container.add(node = TestNode.Container(name = "inner"), listOf("unknown"))
                            }

                        exception.message shouldBe "example > unknown does not exist"
                    }

                    test("add to non-container") {
                        val container = TestNode.Container(name = "example")
                        container.add(TestNode.Test(name = "inner", duration = 5.seconds, status = TestStatus.Success))

                        val exception =
                            shouldThrowExactly<IllegalArgumentException> {
                                container.add(node = TestNode.Container(name = "nested-inner"), listOf("inner"))
                            }

                        exception.message shouldBe "example > inner is not a TestNode.Container"
                    }
                }
            }

            context("type") {
                test("container") {
                    val container = TestNode.Container(name = "example")
                }

                test("test") {
                    val container = TestNode.Test(name = "example", duration = 5.seconds, status = TestStatus.Success)
                }
            }

            context("duration") {
                test("single test") {
                    val test = TestNode.Test(name = "pass", duration = 5.seconds, status = TestStatus.Success)
                    test.duration shouldBe 5.seconds
                }

                test("empty container") {
                    val container = TestNode.Container(name = "example")
                    container.duration shouldBe Duration.ZERO
                }

                test("container with single test") {
                    val container = TestNode.Container(name = "example")
                    container.add(TestNode.Test(name = "pass", duration = 5.seconds, status = TestStatus.Success))
                    container.duration shouldBe 5.seconds
                }

                test("container with nested containers with tests") {
                    val container = TestNode.Container(name = "example")
                    container.add(TestNode.Test(name = "pass", duration = 5.seconds, status = TestStatus.Success))
                    container.add(TestNode.Container("inner"))
                    container.add(TestNode.Test(name = "pass", duration = 10.seconds, status = TestStatus.Success), listOf("inner"))

                    container.duration shouldBe 15.seconds
                }
            }
        }
    })
