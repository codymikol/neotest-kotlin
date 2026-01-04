package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class BehaviorSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("BehaviorSpec Discovery") {
            test("init block") {
                val ktFile =
                    createKtFile(
                        "ExampleBehaviorSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.BehaviorSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleBehaviorSpec : BehaviorSpec() {
                            init {
                                Context("Context") {
                                    Given("Given") {
                                        When("When") {
                                            Then("Then") {
                                                1 shouldBe 1
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleBehaviorSpec",
                            name = "ExampleBehaviorSpec",
                            position =
                            Position(
                                filename = "/ExampleBehaviorSpec.kt",
                                start = 6,
                                end = 18,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleBehaviorSpec::Context",
                                    name = "Context",
                                    position =
                                    Position(
                                        filename = "/ExampleBehaviorSpec.kt",
                                        start = 8,
                                        end = 16,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleBehaviorSpec::Context::Given",
                                            name = "Given",
                                            position =
                                            Position(
                                                filename = "/ExampleBehaviorSpec.kt",
                                                start = 9,
                                                end = 15,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleBehaviorSpec::Context::Given::When",
                                                    name = "When",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleBehaviorSpec.kt",
                                                        start = 10,
                                                        end = 14,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleBehaviorSpec::Context::Given::When::Then",
                                                            name = "Then",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                start = 11,
                                                                end = 13,
                                                            ),
                                                        ),
                                                    ),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("basic test - All Uppercase") {
                val ktFile =
                    createKtFile(
                        "ExampleBehaviorSpec.kt",
                        """
                        package org.example
                                
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
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleBehaviorSpec",
                            name = "ExampleBehaviorSpec",
                            position =
                            Position(
                                filename = "/ExampleBehaviorSpec.kt",
                                start = 6,
                                end = 16,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleBehaviorSpec::Context",
                                    name = "Context",
                                    position =
                                    Position(
                                        filename = "/ExampleBehaviorSpec.kt",
                                        start = 7,
                                        end = 15,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleBehaviorSpec::Context::Given",
                                            name = "Given",
                                            position =
                                            Position(
                                                filename = "/ExampleBehaviorSpec.kt",
                                                start = 8,
                                                end = 14,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleBehaviorSpec::Context::Given::When",
                                                    name = "When",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleBehaviorSpec.kt",
                                                        start = 9,
                                                        end = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleBehaviorSpec::Context::Given::When::Then",
                                                            name = "Then",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                start = 10,
                                                                end = 12,
                                                            ),
                                                        ),
                                                    ),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("basic test - All Lowercase") {
                val ktFile =
                    createKtFile(
                        "ExampleBehaviorSpec.kt",
                        """
                        package org.example
                                
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
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleBehaviorSpec",
                            name = "ExampleBehaviorSpec",
                            position =
                            Position(
                                filename = "/ExampleBehaviorSpec.kt",
                                start = 6,
                                end = 16,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleBehaviorSpec::context",
                                    name = "context",
                                    position =
                                    Position(
                                        filename = "/ExampleBehaviorSpec.kt",
                                        start = 7,
                                        end = 15,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleBehaviorSpec::context::given",
                                            name = "given",
                                            position =
                                            Position(
                                                filename = "/ExampleBehaviorSpec.kt",
                                                start = 8,
                                                end = 14,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleBehaviorSpec::context::given::when",
                                                    name = "when",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleBehaviorSpec.kt",
                                                        start = 9,
                                                        end = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleBehaviorSpec::context::given::when::then",
                                                            name = "then",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                start = 10,
                                                                end = 12,
                                                            ),
                                                        ),
                                                    ),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("complex test") {
                val ktFile =
                    createKtFile(
                        "ExampleBehaviorSpec.kt",
                        """
                        package org.example
                                
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
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleBehaviorSpec",
                            name = "ExampleBehaviorSpec",
                            position =
                            Position(
                                filename = "/ExampleBehaviorSpec.kt",
                                start = 6,
                                end = 36,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleBehaviorSpec::Context",
                                    name = "Context",
                                    position =
                                    Position(
                                        filename = "/ExampleBehaviorSpec.kt",
                                        start = 7,
                                        end = 35,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleBehaviorSpec::Context::given",
                                            name = "given",
                                            position =
                                            Position(
                                                filename = "/ExampleBehaviorSpec.kt",
                                                start = 8,
                                                end = 26,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleBehaviorSpec::Context::given::when",
                                                    name = "when",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleBehaviorSpec.kt",
                                                        start = 9,
                                                        end = 25,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleBehaviorSpec::Context::given::when::then",
                                                            name = "then",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                start = 10,
                                                                end = 12,
                                                            ),
                                                        ),
                                                        Discovered.Container(
                                                            id = "org.example.ExampleBehaviorSpec::Context::given::when::and",
                                                            name = "and",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                start = 14,
                                                                end = 18,
                                                            ),
                                                            tests =
                                                            setOf(
                                                                Discovered.Test(
                                                                    id = "org.example.ExampleBehaviorSpec::Context::given::when::and::then1",
                                                                    name = "then1",
                                                                    position =
                                                                    Position(
                                                                        filename = "/ExampleBehaviorSpec.kt",
                                                                        start = 15,
                                                                        end = 17,
                                                                    ),
                                                                ),
                                                            ),
                                                        ),
                                                        Discovered.Container(
                                                            id = "org.example.ExampleBehaviorSpec::Context::given::when::And",
                                                            name = "And",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                start = 20,
                                                                end = 24,
                                                            ),
                                                            tests =
                                                            setOf(
                                                                Discovered.Test(
                                                                    id = "org.example.ExampleBehaviorSpec::Context::given::when::And::then2",
                                                                    name = "then2",
                                                                    position =
                                                                    Position(
                                                                        filename = "/ExampleBehaviorSpec.kt",
                                                                        start = 21,
                                                                        end = 23,
                                                                    ),
                                                                ),
                                                            ),
                                                        ),
                                                    ),
                                                ),
                                            ),
                                        ),
                                        Discovered.Container(
                                            id = "org.example.ExampleBehaviorSpec::Context::Given",
                                            name = "Given",
                                            position =
                                            Position(
                                                filename = "/ExampleBehaviorSpec.kt",
                                                start = 28,
                                                end = 34,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleBehaviorSpec::Context::Given::When",
                                                    name = "When",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleBehaviorSpec.kt",
                                                        start = 29,
                                                        end = 33,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleBehaviorSpec::Context::Given::When::Then",
                                                            name = "Then",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                start = 30,
                                                                end = 32,
                                                            ),
                                                        ),
                                                    ),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
            }
        }
    })
