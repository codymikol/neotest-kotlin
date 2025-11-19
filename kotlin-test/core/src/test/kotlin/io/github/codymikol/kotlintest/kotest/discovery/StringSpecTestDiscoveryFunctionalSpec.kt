package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.Position
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringSpecTestDiscoveryFunctionalSpec : FunSpec ({
    context("StringSpec Discovery") {
        test("single test") {
            val ktFile = createKtFile(
                "ExampleStringSpec.kt", """
            import io.kotest.core.spec.style.StringSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleShouldSpec : StringSpec({
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
                        filename = "/ExampleStringSpec.kt",
                        start = 5,
                        end = 7
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("multiple top-level test") {
            val ktFile = createKtFile(
                "ExampleStringSpec.kt", """
            import io.kotest.core.spec.style.StringSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleShouldSpec : StringSpec({
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
                    id = "example string",
                    position = Position(
                        filename = "/ExampleStringSpec.kt",
                        start = 5,
                        end = 7
                    ),
                    type = TestType.TEST
                ),
                DiscoveredTest(
                    id = "other example string",
                    position = Position(
                        filename = "/ExampleStringSpec.kt",
                        start = 9,
                        end = 11
                    ),
                    type = TestType.TEST
                )
            )
        }
    }
})