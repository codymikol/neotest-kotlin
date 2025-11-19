package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.Position
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WordSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("WordSpec Discovery") {
        test("single test") {
            val ktFile = createKtFile(
                "ExampleWordSpec.kt",
                """
            package org.example    

            import io.kotest.core.spec.style.WordSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleWordSpec : WordSpec({
                "example string" {
                    1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleWordSpec",
                    position = Position(
                        filename = "/ExampleWordSpec.kt",
                        start = 6,
                        end = 10
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleWordSpec::example string",
                    position = Position(
                        filename = "/ExampleWordSpec.kt",
                        start = 7,
                        end = 9
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("invalid container") {
            val ktFile = createKtFile(
                "ExampleWordSpec.kt",
                """
            package org.example    

            import io.kotest.core.spec.style.WordSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleWordSpec : WordSpec({
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
                    id = "org.example.ExampleWordSpec",
                    position = Position(
                        filename = "/ExampleWordSpec.kt",
                        start = 6,
                        end = 12
                    ),
                    type = TestType.CONTAINER
                ),
            )
        }

        test("nested test") {
            val ktFile = createKtFile(
                "ExampleWordSpec.kt",
                """
            package org.example    

            import io.kotest.core.spec.style.WordSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleWordSpec : WordSpec({
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
                    id = "org.example.ExampleWordSpec",
                    position = Position(
                        filename = "/ExampleWordSpec.kt",
                        start = 6,
                        end = 12
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleWordSpec::container",
                    position = Position(
                        filename = "/ExampleWordSpec.kt",
                        start = 7,
                        end = 11
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleWordSpec::container::example string",
                    position = Position(
                        filename = "/ExampleWordSpec.kt",
                        start = 8,
                        end = 10
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("deeply nested test") {
            val ktFile = createKtFile(
                "ExampleWordSpec.kt",
                """
            package org.example    

            import io.kotest.core.spec.style.WordSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleWordSpec : WordSpec({
                "container" should {
                    "container1" should {
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
                    id = "org.example.ExampleWordSpec",
                    position = Position(
                        filename = "/ExampleWordSpec.kt",
                        start = 6,
                        end = 14
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleWordSpec::container",
                    position = Position(
                        filename = "/ExampleWordSpec.kt",
                        start = 7,
                        end = 13
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleWordSpec::container::container1",
                    position = Position(
                        filename = "/ExampleWordSpec.kt",
                        start = 8,
                        end = 12
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleWordSpec::container::container1::example string",
                    position = Position(
                        filename = "/ExampleWordSpec.kt",
                        start = 9,
                        end = 11
                    ),
                    type = TestType.TEST
                )
            )
        }
    }
})
