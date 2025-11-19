package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.Position
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("StringSpec Discovery") {
        test("single test") {
            val ktFile = createKtFile(
                "ExampleStringSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.StringSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleStringSpec : StringSpec({
                "example string" {
                    1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleStringSpec",
                    position = Position(
                        filename = "/ExampleStringSpec.kt",
                        start = 6,
                        end = 10
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleStringSpec::example string",
                    position = Position(
                        filename = "/ExampleStringSpec.kt",
                        start = 7,
                        end = 9
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("multiple top-level test") {
            val ktFile = createKtFile(
                "ExampleStringSpec.kt",
                """
            package org.example

            import io.kotest.core.spec.style.StringSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleStringSpec : StringSpec({
                "example string" {
                    1 shouldBe 1
                }
                
                "other example string" {
                    1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleStringSpec",
                    position = Position(
                        filename = "/ExampleStringSpec.kt",
                        start = 6,
                        end = 14
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleStringSpec::example string",
                    position = Position(
                        filename = "/ExampleStringSpec.kt",
                        start = 7,
                        end = 9
                    ),
                    type = TestType.TEST
                ),
                DiscoveredTest(
                    id = "org.example.ExampleStringSpec::other example string",
                    position = Position(
                        filename = "/ExampleStringSpec.kt",
                        start = 11,
                        end = 13
                    ),
                    type = TestType.TEST
                )
            )
        }
    }
})
