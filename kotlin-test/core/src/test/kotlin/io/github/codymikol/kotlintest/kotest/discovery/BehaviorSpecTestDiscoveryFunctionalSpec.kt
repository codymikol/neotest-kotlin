package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.Position
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BehaviorSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("BehaviorSpec Discovery") {
        test("basic test - All Uppercase") {
            val ktFile = createKtFile(
                "ExampleBehaviorSpec.kt",
                """
            import io.kotest.core.spec.style.BehaviorSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleBehaviorSpec : BehaviorSpec({
                Context("Context") {
                    Given("Given") {
                        When("When") {
                            Then("Then") {
                                1 shouldBe 1
                            }
                        }
                    }
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "Context",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 5,
                        end = 13
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "Context::Given",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 6,
                        end = 12
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "Context::Given::When",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 7,
                        end = 11
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "Context::Given::When::Then",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 8,
                        end = 10
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("basic test - All Lowercase") {
            val ktFile = createKtFile(
                "ExampleBehaviorSpec.kt",
                """
            import io.kotest.core.spec.style.BehaviorSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleBehaviorSpec : BehaviorSpec({
                context("context") {
                    given("given") {
                        `when`("when") {
                            then("then") {
                                1 shouldBe 1
                            }
                        }
                    }
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "context",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 5,
                        end = 13
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "context::given",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 6,
                        end = 12
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "context::given::when",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 7,
                        end = 11
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "context::given::when::then",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 8,
                        end = 10
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("complex test") {
            val ktFile = createKtFile(
                "ExampleBehaviorSpec.kt",
                """
            import io.kotest.core.spec.style.BehaviorSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleBehaviorSpec : BehaviorSpec({
                Context("Context") {
                    given("given") {
                        `when`("when") {
                            then("then") {
                                1 shouldBe 1
                            }
                            
                            and("and") {
                                then("then1") {
                                    1 shouldBe 1
                                }
                            }
                            
                            And("And") {
                                then("then2") {
                                    1 shouldBe 1
                                }
                            }
                        }
                    }
                
                    Given("Given") {
                        When("When") {
                            Then("Then") {
                                1 shouldBe 1
                            }
                        }
                    }
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "Context",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 5,
                        end = 33
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "Context::given",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 6,
                        end = 24
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "Context::given::when",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 7,
                        end = 23
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "Context::given::when::then",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 8,
                        end = 10
                    ),
                    type = TestType.TEST
                ),
                DiscoveredTest(
                    id = "Context::given::when::and",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 12,
                        end = 16
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "Context::given::when::and::then1",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 13,
                        end = 15
                    ),
                    type = TestType.TEST
                ),
                DiscoveredTest(
                    id = "Context::given::when::And",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 18,
                        end = 22
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "Context::given::when::And::then2",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 19,
                        end = 21
                    ),
                    type = TestType.TEST
                ),
                DiscoveredTest(
                    id = "Context::Given",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 26,
                        end = 32
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "Context::Given::When",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 27,
                        end = 31
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "Context::Given::When::Then",
                    position = Position(
                        filename = "/ExampleBehaviorSpec.kt",
                        start = 28,
                        end = 30
                    ),
                    type = TestType.TEST
                )
            )
        }
    }
})
