package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.Position
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FreeSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("FreeSpec Discovery") {
        test("single test") {
            val ktFile = createKtFile(
                "ExampleFreeSpec.kt",
                """
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
                    id = "example string",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 5,
                        end = 7
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("invalid container") {
            val ktFile = createKtFile(
                "ExampleFreeSpec.kt",
                """
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

            results shouldBe emptySet()
        }

        test("nested test") {
            val ktFile = createKtFile(
                "ExampleFreeSpec.kt",
                """
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
                    id = "container",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 5,
                        end = 9
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "container::example string",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 6,
                        end = 8
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("deeply nested test") {
            val ktFile = createKtFile(
                "ExampleFreeSpec.kt",
                """
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
                    id = "container",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 5,
                        end = 11
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "container::container1",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 6,
                        end = 10
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "container::container1::example string",
                    position = Position(
                        filename = "/ExampleFreeSpec.kt",
                        start = 7,
                        end = 9
                    ),
                    type = TestType.TEST
                )
            )
        }
    }
})
