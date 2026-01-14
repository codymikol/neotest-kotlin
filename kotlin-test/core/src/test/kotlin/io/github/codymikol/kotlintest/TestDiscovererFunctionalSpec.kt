package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Position
import io.github.codymikol.kotlintest.discover.model.TestWarning
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class TestDiscovererFunctionalSpec : FunSpec({
    context("functional") {
        test("duplicate test names") {
            val ktFile =
                createKtFile(
                    "ExampleFunSpec.kt",
                    """
                        package org.example

                        import io.kotest.core.spec.style.FunSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFunSpec : FunSpec({
                            test("test") {
                              1 shouldBe 1
                            }
                            
                            test("test") {
                              1 shouldBe 1
                            }
                            
                            test("test") {
                              1 shouldBe 1
                            }
                        })
                    """.trimIndent(),
                )

            val result = TestDiscoverer.discoverAllTests(files = setOf(ktFile))

            result.should {
                it.warnings shouldBe listOf(
                    TestWarning(
                        message = "Multiple tests defined with name 'test'",
                        position = Position(
                            filename = "/ExampleFunSpec.kt",
                            startLine = 11,
                            startColumn = 5,
                            endLine = 13,
                            endColumn = 5
                        )
                    ),
                    TestWarning(
                        message = "Multiple tests defined with name 'test'",
                        position = Position(
                            filename = "/ExampleFunSpec.kt",
                            startLine = 15,
                            startColumn = 5,
                            endLine = 17,
                            endColumn = 5
                        )
                    ),
                )
            }
        }
    }
})
