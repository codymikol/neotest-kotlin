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
                                startLine = 6,
                                endLine = 18,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleBehaviorSpec::Context",
                                    name = "Context",
                                    position =
                                    Position(
                                        filename = "/ExampleBehaviorSpec.kt",
                                        startLine = 8,
                                        endLine = 16,
                                        startColumn = 9,
                                        endColumn = 9,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleBehaviorSpec::Context::Given",
                                            name = "Given",
                                            position =
                                            Position(
                                                filename = "/ExampleBehaviorSpec.kt",
                                                startLine = 9,
                                                endLine = 15,
                                                startColumn = 13,
                                                endColumn = 13,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleBehaviorSpec::Context::Given::When",
                                                    name = "When",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleBehaviorSpec.kt",
                                                        startLine = 10,
                                                        endLine = 14,
                                                        startColumn = 17,
                                                        endColumn = 17,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleBehaviorSpec::Context::Given::When::Then",
                                                            name = "Then",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                startLine = 11,
                                                                endLine = 13,
                                                                startColumn = 21,
                                                                endColumn = 21,
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
                                startLine = 6,
                                endLine = 16,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleBehaviorSpec::Context",
                                    name = "Context",
                                    position =
                                    Position(
                                        filename = "/ExampleBehaviorSpec.kt",
                                        startLine = 7,
                                        endLine = 15,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleBehaviorSpec::Context::Given",
                                            name = "Given",
                                            position =
                                            Position(
                                                filename = "/ExampleBehaviorSpec.kt",
                                                startLine = 8,
                                                endLine = 14,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleBehaviorSpec::Context::Given::When",
                                                    name = "When",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleBehaviorSpec.kt",
                                                        startLine = 9,
                                                        endLine = 13,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleBehaviorSpec::Context::Given::When::Then",
                                                            name = "Then",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                startLine = 10,
                                                                endLine = 12,
                                                                startColumn = 17,
                                                                endColumn = 17,
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
                                startLine = 6,
                                endLine = 16,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleBehaviorSpec::context",
                                    name = "context",
                                    position =
                                    Position(
                                        filename = "/ExampleBehaviorSpec.kt",
                                        startLine = 7,
                                        endLine = 15,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleBehaviorSpec::context::given",
                                            name = "given",
                                            position =
                                            Position(
                                                filename = "/ExampleBehaviorSpec.kt",
                                                startLine = 8,
                                                endLine = 14,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleBehaviorSpec::context::given::when",
                                                    name = "when",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleBehaviorSpec.kt",
                                                        startLine = 9,
                                                        endLine = 13,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleBehaviorSpec::context::given::when::then",
                                                            name = "then",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                startLine = 10,
                                                                endLine = 12,
                                                                startColumn = 17,
                                                                endColumn = 17,
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
                                startLine = 6,
                                endLine = 36,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleBehaviorSpec::Context",
                                    name = "Context",
                                    position =
                                    Position(
                                        filename = "/ExampleBehaviorSpec.kt",
                                        startLine = 7,
                                        endLine = 35,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleBehaviorSpec::Context::given",
                                            name = "given",
                                            position =
                                            Position(
                                                filename = "/ExampleBehaviorSpec.kt",
                                                startLine = 8,
                                                endLine = 26,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleBehaviorSpec::Context::given::when",
                                                    name = "when",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleBehaviorSpec.kt",
                                                        startLine = 9,
                                                        endLine = 25,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleBehaviorSpec::Context::given::when::then",
                                                            name = "then",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                startLine = 10,
                                                                endLine = 12,
                                                                startColumn = 17,
                                                                endColumn = 17,
                                                            ),
                                                        ),
                                                        Discovered.Container(
                                                            id = "org.example.ExampleBehaviorSpec::Context::given::when::and",
                                                            name = "and",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                startLine = 14,
                                                                endLine = 18,
                                                                startColumn = 17,
                                                                endColumn = 17,
                                                            ),
                                                            tests =
                                                            setOf(
                                                                Discovered.Test(
                                                                    id = "org.example.ExampleBehaviorSpec::Context::given::when::and::then1",
                                                                    name = "then1",
                                                                    position =
                                                                    Position(
                                                                        filename = "/ExampleBehaviorSpec.kt",
                                                                        startLine = 15,
                                                                        endLine = 17,
                                                                        startColumn = 21,
                                                                        endColumn = 21,
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
                                                                startLine = 20,
                                                                endLine = 24,
                                                                startColumn = 17,
                                                                endColumn = 17,
                                                            ),
                                                            tests =
                                                            setOf(
                                                                Discovered.Test(
                                                                    id = "org.example.ExampleBehaviorSpec::Context::given::when::And::then2",
                                                                    name = "then2",
                                                                    position =
                                                                    Position(
                                                                        filename = "/ExampleBehaviorSpec.kt",
                                                                        startLine = 21,
                                                                        endLine = 23,
                                                                        startColumn = 21,
                                                                        endColumn = 21,
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
                                                startLine = 28,
                                                endLine = 34,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleBehaviorSpec::Context::Given::When",
                                                    name = "When",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleBehaviorSpec.kt",
                                                        startLine = 29,
                                                        endLine = 33,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleBehaviorSpec::Context::Given::When::Then",
                                                            name = "Then",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleBehaviorSpec.kt",
                                                                startLine = 30,
                                                                endLine = 32,
                                                                startColumn = 17,
                                                                endColumn = 17,
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
