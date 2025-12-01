package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.DiscoveredTest
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.TestType
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FunSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("FunSpec Discovery") {
        test("top-level test") {
            val ktFile = createKtFile(
                "ExampleFunSpec.kt",
                """
            package org.example
                    
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
                    id = "org.example.ExampleFunSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 6,
                        end = 10
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 7,
                        end = 9
                    )
                )
            )
        }

        test("nested test") {
            val ktFile = createKtFile(
                "ExampleFunSpec.kt",
                """
            package org.example
            
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
                    id = "org.example.ExampleFunSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 6,
                        end = 12
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 7,
                        end = 11
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 8,
                        end = 10
                    )
                )
            )
        }

        test("deeply nested test") {
            val ktFile = createKtFile(
                "ExampleFunSpec.kt",
                """
            package org.example
                    
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
                    id = "org.example.ExampleFunSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 6,
                        end = 16
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container1",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 7,
                        end = 15
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container1::container2",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 8,
                        end = 14
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container1::container2::container3",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 9,
                        end = 13
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container1::container2::container3::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 10,
                        end = 12
                    )
                )
            )
        }

        test("multiple top-level test") {
            val ktFile = createKtFile(
                "ExampleFunSpec.kt",
                """
            package org.example
                    
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
                    id = "org.example.ExampleFunSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 6,
                        end = 18
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 7,
                        end = 9
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::test1",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 11,
                        end = 13
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::test2",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 15,
                        end = 17
                    )
                )
            )
        }

        test("complex test suite") {
            val ktFile = createKtFile(
                "ExampleFunSpec.kt",
                """
            package org.example
                    
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
                    id = "org.example.ExampleFunSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 6,
                        end = 28
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container1",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 7,
                        end = 11
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container1::test1",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 8,
                        end = 10
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container2",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 13,
                        end = 23
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container2::container3",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 14,
                        end = 18
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container2::container3::test2",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 15,
                        end = 17
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::container2::test3",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 20,
                        end = 22
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFunSpec::test4",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleFunSpec.kt",
                        start = 25,
                        end = 27
                    )
                ),
            )
        }
    }
})
