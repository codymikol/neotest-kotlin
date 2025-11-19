package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.Position
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FunSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("FunSpec Discovery") {
        test("top-level test") {
            val ktFile = createKtFile(
                "ExampleFunSpec.kt",
                """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFunSpec : FunSpec({
                test("test") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 5,
                        end = 7
                    )
                )
            )
        }

        test("nested test") {
            val ktFile = createKtFile(
                "ExampleFunSpec.kt",
                """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFunSpec : FunSpec({
                context("container") {
                    test("test") {
                      1 shouldBe 1
                    }
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "container",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 5,
                        end = 9
                    )
                ),
                DiscoveredTest(
                    id = "container::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 6,
                        end = 8
                    )
                )
            )
        }

        test("deeply nested test") {
            val ktFile = createKtFile(
                "ExampleFunSpec.kt",
                """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFunSpec : FunSpec({
                context("container1") {
                    context("container2") {
                        context("container3") {
                            test("test") {
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
                    id = "container1",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 5,
                        end = 13
                    )
                ),
                DiscoveredTest(
                    id = "container1::container2",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 6,
                        end = 12
                    )
                ),
                DiscoveredTest(
                    id = "container1::container2::container3",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 7,
                        end = 11
                    )
                ),
                DiscoveredTest(
                    id = "container1::container2::container3::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 8,
                        end = 10
                    )
                )
            )
        }

        test("multiple top-level test") {
            val ktFile = createKtFile(
                "ExampleFunSpec.kt",
                """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFunSpec : FunSpec({
                test("test") {
                  1 shouldBe 1
                }
                
                test("test1") {
                  1 shouldBe 1
                }
                
                test("test2") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 5,
                        end = 7
                    )
                ),
                DiscoveredTest(
                    id = "test1",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 9,
                        end = 11
                    )
                ),
                DiscoveredTest(
                    id = "test2",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 13,
                        end = 15
                    )
                )
            )
        }

        test("complex test suite") {
            val ktFile = createKtFile(
                "ExampleFunSpec.kt",
                """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFunSpec : FunSpec({
                context("container1") {
                    test("test1") {
                        1 shouldBe 1
                    }
                }
                
                context("container2") {
                    context("container3") {
                        test("test2") {
                            1 shouldBe 1
                        }
                    }
                    
                    test("test3") {
                        1 shouldBe 1
                    }
                }
                
                test("test4") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "container1",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 5,
                        end = 9
                    )
                ),
                DiscoveredTest(
                    id = "container1::test1",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 6,
                        end = 8
                    )
                ),
                DiscoveredTest(
                    id = "container2",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 11,
                        end = 21
                    )
                ),
                DiscoveredTest(
                    id = "container2::container3",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 12,
                        end = 16
                    )
                ),
                DiscoveredTest(
                    id = "container2::container3::test2",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 13,
                        end = 15
                    )
                ),
                DiscoveredTest(
                    id = "container2::test3",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 18,
                        end = 20
                    )
                ),
                DiscoveredTest(
                    id = "test4",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 23,
                        end = 25
                    )
                ),
            )
        }
    }
})
