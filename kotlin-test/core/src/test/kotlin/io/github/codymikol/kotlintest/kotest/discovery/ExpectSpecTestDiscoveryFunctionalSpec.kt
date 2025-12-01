package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.DiscoveredTest
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.TestType
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ExpectSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("ExpectSpec Discovery") {
        test("top-level test") {
            val ktFile = createKtFile(
                "ExampleExpectSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.ExpectSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleExpectSpec : ExpectSpec({
                expect("test") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 6,
                        end = 10
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 7,
                        end = 9
                    )
                )
            )
        }

        test("nested test") {
            val ktFile = createKtFile(
                "ExampleExpectSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.ExpectSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleExpectSpec : ExpectSpec({
                context("container") {
                    expect("test") {
                      1 shouldBe 1
                    }
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 6,
                        end = 12
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 7,
                        end = 11
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 8,
                        end = 10
                    )
                )
            )
        }

        test("deeply nested test") {
            val ktFile = createKtFile(
                "ExampleExpectSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.ExpectSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleExpectSpec : ExpectSpec({
                context("container1") {
                    context("container2") {
                        context("container3") {
                            expect("test") {
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
                    id = "org.example.ExampleExpectSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 6,
                        end = 16
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container1",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 7,
                        end = 15
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container1::container2",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 8,
                        end = 14
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container1::container2::container3",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 9,
                        end = 13
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container1::container2::container3::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 10,
                        end = 12
                    )
                )
            )
        }

        test("multiple top-level test") {
            val ktFile = createKtFile(
                "ExampleExpectSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.ExpectSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleExpectSpec : ExpectSpec({
                expect("test") {
                  1 shouldBe 1
                }
                
                expect("test1") {
                  1 shouldBe 1
                }
                
                expect("test2") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 6,
                        end = 18
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 7,
                        end = 9
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::test1",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 11,
                        end = 13
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::test2",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 15,
                        end = 17
                    )
                )
            )
        }

        test("complex test suite") {
            val ktFile = createKtFile(
                "ExampleExpectSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.ExpectSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleExpectSpec : ExpectSpec({
                context("container1") {
                    expect("test1") {
                        1 shouldBe 1
                    }
                }
                
                context("container2") {
                    context("container3") {
                        expect("test2") {
                            1 shouldBe 1
                        }
                    }
                    
                    expect("test3") {
                        1 shouldBe 1
                    }
                }
                
                expect("test4") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 6,
                        end = 28
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container1",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 7,
                        end = 11
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container1::test1",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 8,
                        end = 10
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container2",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 13,
                        end = 23
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container2::container3",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 14,
                        end = 18
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container2::container3::test2",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 15,
                        end = 17
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::container2::test3",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 20,
                        end = 22
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleExpectSpec::test4",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleExpectSpec.kt",
                        start = 25,
                        end = 27
                    )
                ),
            )
        }
    }
})
