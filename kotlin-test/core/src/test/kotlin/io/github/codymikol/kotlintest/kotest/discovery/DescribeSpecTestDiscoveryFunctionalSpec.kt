package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.Position
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DescribeSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("DescribeSpec Discovery") {
        test("top-level test") {
            val ktFile = createKtFile(
                "ExampleDescribeSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.DescribeSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleDescribeSpec : DescribeSpec({
                it("test") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 6,
                        end = 10
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 7,
                        end = 9
                    )
                )
            )
        }

        test("nested test") {
            val ktFile = createKtFile(
                "ExampleDescribeSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.DescribeSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleDescribeSpec : DescribeSpec({
                describe("container") {
                    it("test") {
                      1 shouldBe 1
                    }
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 6,
                        end = 12
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 7,
                        end = 11
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 8,
                        end = 10
                    )
                )
            )
        }

        test("deeply nested test") {
            val ktFile = createKtFile(
                "ExampleDescribeSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.DescribeSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleDescribeSpec : DescribeSpec({
                describe("container1") {
                    describe("container2") {
                        describe("container3") {
                            it("test") {
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
                    id = "org.example.ExampleDescribeSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 6,
                        end = 16
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container1",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 7,
                        end = 15
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container1::container2",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 8,
                        end = 14
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container1::container2::container3",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 9,
                        end = 13
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container1::container2::container3::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 10,
                        end = 12
                    )
                )
            )
        }

        test("multiple top-level test") {
            val ktFile = createKtFile(
                "ExampleDescribeSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.DescribeSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleDescribeSpec : DescribeSpec({
                it("test") {
                  1 shouldBe 1
                }
                
                it("test1") {
                  1 shouldBe 1
                }
                
                it("test2") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 6,
                        end = 18
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::test",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 7,
                        end = 9
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::test1",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 11,
                        end = 13
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::test2",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 15,
                        end = 17
                    )
                )
            )
        }

        test("complex test suite") {
            val ktFile = createKtFile(
                "ExampleDescribeSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.DescribeSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleDescribeSpec : DescribeSpec({
                describe("container1") {
                    it("test1") {
                        1 shouldBe 1
                    }
                }
                
                describe("container2") {
                    describe("container3") {
                        it("test2") {
                            1 shouldBe 1
                        }
                    }
                    
                    it("test3") {
                        1 shouldBe 1
                    }
                }
                
                it("test4") {
                  1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 6,
                        end = 28
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container1",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 7,
                        end = 11
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container1::test1",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 8,
                        end = 10
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container2",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 13,
                        end = 23
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container2::container3",
                    type = TestType.CONTAINER,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 14,
                        end = 18
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container2::container3::test2",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 15,
                        end = 17
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::container2::test3",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 20,
                        end = 22
                    )
                ),
                DiscoveredTest(
                    id = "org.example.ExampleDescribeSpec::test4",
                    type = TestType.TEST,
                    position = Position(
                        filename = "/ExampleDescribeSpec.kt",
                        start = 25,
                        end = 27
                    )
                ),
            )
        }
    }
})
