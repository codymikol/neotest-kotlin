package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.DiscoveredTest
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.TestType
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FreeSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("FreeSpec Discovery") {
        test("single test") {
            val ktFile = createKtFile(
                "ExampleFreeSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.FreeSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFreeSpec : FreeSpec({
                "example string" {
                    1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleFreeSpec",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 6,
                        end = 10
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFreeSpec::example string",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 7,
                        end = 9
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("invalid container") {
            val ktFile = createKtFile(
                "ExampleFreeSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.FreeSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFreeSpec : FreeSpec({
                "container" should {
                    "example string" {
                        1 shouldBe 1
                    }
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleFreeSpec",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 6,
                        end = 12
                    ),
                    type = TestType.CONTAINER
                ),
            )
        }

        test("nested test") {
            val ktFile = createKtFile(
                "ExampleFreeSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.FreeSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFreeSpec : FreeSpec({
                "container" - {
                    "example string" {
                        1 shouldBe 1
                    }
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleFreeSpec",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 6,
                        end = 12
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFreeSpec::container",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 7,
                        end = 11
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFreeSpec::container::example string",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 8,
                        end = 10
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("deeply nested test") {
            val ktFile = createKtFile(
                "ExampleFreeSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.FreeSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFreeSpec : FreeSpec({
                "container" - {
                    "container1" - {
                        "example string" {
                            1 shouldBe 1
                        }
                    }
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleFreeSpec",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 6,
                        end = 14
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFreeSpec::container",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 7,
                        end = 13
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFreeSpec::container::container1",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 8,
                        end = 12
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleFreeSpec::container::container1::example string",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 9,
                        end = 11
                    ),
                    type = TestType.TEST
                )
            )
        }
    }
})
