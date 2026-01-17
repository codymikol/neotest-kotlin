package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.DiscoveredResult
import io.github.codymikol.kotlintest.discover.model.Position
import io.github.codymikol.kotlintest.discover.model.TestWarning
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class TestDiscovererFunctionalSpec : FunSpec({
    context("functional") {
        test("JUnit + KoTest") {
            val ktFile =
                createKtFile(
                    "ExampleTest.kt",
                    """
                        package org.example
                                
                        import io.kotest.core.spec.style.FunSpec
                        import io.kotest.matchers.shouldBe
                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Test
                        
                        class ExampleTest : FunSpec({
                            test("test") {
                              1 shouldBe 1
                            }
                        }) {
                            @Test
                            fun `custom name`() {
                                assertEquals(1, 1)
                            }
                        }
                    """.trimIndent(),
                )

            val result = TestDiscoverer.discoverAllTests(setOf(ktFile))

            result shouldBe DiscoveredResult(
                warnings = emptyList(),
                tests = setOf(
                    Discovered.Container(
                        id = "org.example.ExampleTest",
                        name = "ExampleTest",
                        position = Position(
                            filename = "/ExampleTest.kt",
                            startLine = 8,
                            startColumn = 1,
                            endLine = 17,
                            endColumn = 1,
                        ),
                        tests = setOf(
                            Discovered.Test(
                                id = "org.example.ExampleTest::test",
                                name = "test",
                                position = Position(
                                    filename = "/ExampleTest.kt",
                                    startLine = 9,
                                    startColumn = 5,
                                    endLine = 11,
                                    endColumn = 5,
                                )
                            ),
                            Discovered.Test(
                                id = "org.example.ExampleTest::custom name",
                                name = "custom name",
                                position = Position(
                                    filename = "/ExampleTest.kt",
                                    startLine = 14,
                                    startColumn = 5,
                                    endLine = 16,
                                    endColumn = 5,
                                )
                            )
                        )
                    ),
                )
            )
        }

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
