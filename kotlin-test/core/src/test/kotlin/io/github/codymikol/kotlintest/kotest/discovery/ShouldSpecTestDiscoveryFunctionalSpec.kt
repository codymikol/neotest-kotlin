package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.DiscoveredTest
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.TestType
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ShouldSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("ShouldSpec Discovery") {
        test("top-level test") {
            val ktFile = createKtFile(
                "ExampleShouldSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.ShouldSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleShouldSpec : ShouldSpec({
                should("should") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 6,
                        end = 10
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::should",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 7,
                        end = 9
                    )
                )
            )
        }

        test("nested test") {
            val ktFile = createKtFile(
                "ExampleShouldSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.ShouldSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleShouldSpec : ShouldSpec({
                context("container") {
                    should("should") {
                      1 shouldBe 1
                    }
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 6,
                        end = 12
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 7,
                        end = 11
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container::should",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 8,
                        end = 10
                    )
                )
            )
        }

        test("deeply nested test") {
            val ktFile = createKtFile(
                "ExampleShouldSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.ShouldSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleShouldSpec : ShouldSpec({
                context("container1") {
                    context("container2") {
                        context("container3") {
                            should("should") {
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
                    id = "org.example.ExampleShouldSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 6,
                        end = 16
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container1",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 7,
                        end = 15
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container1::container2",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 8,
                        end = 14
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container1::container2::container3",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 9,
                        end = 13
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container1::container2::container3::should",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 10,
                        end = 12
                    )
                )
            )
        }

        test("multiple top-level test") {
            val ktFile = createKtFile(
                "ExampleShouldSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.ShouldSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleShouldSpec : ShouldSpec({
                should("should") {
                  1 shouldBe 1
                }
                
                should("should1") {
                  1 shouldBe 1
                }
                
                should("should2") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 6,
                        end = 18
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::should",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 7,
                        end = 9
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::should1",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 11,
                        end = 13
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::should2",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 15,
                        end = 17
                    )
                )
            )
        }

        test("complex test suite") {
            val ktFile = createKtFile(
                "ExampleShouldSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.ShouldSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleShouldSpec : ShouldSpec({
                context("container1") {
                    should("should1") {
                        1 shouldBe 1
                    }
                }
                
                context("container2") {
                    context("container3") {
                        should("should2") {
                            1 shouldBe 1
                        }
                    }
                    
                    should("should3") {
                        1 shouldBe 1
                    }
                }
                
                should("should4") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 6,
                        end = 28
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container1",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 7,
                        end = 11
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container1::should1",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 8,
                        end = 10
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container2",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 13,
                        end = 23
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container2::container3",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 14,
                        end = 18
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container2::container3::should2",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 15,
                        end = 17
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::container2::should3",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 20,
                        end = 22
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleShouldSpec::should4",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleShouldSpec.kt",
                        start = 25,
                        end = 27
                    )
                ),
            )
        }
    }
})
