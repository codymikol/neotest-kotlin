package io.github.codymikol.kotlintest.kotlintest

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.junit.JUnitTestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.Position
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class KotlinTestDiscoveryFunctionalSpec :
    FunSpec({
        context("Kotlin Test Discovery") {
            test("single test") {
                val ktFile =
                    createKtFile(
                        "ExampleKotlinTest.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import kotlin.test.Test
                        
                        class ExampleKotlinTest {
                            @Test
                            fun test() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val result = JUnitTestDiscoverer.discoverTests(ktFile)

                result.warnings.shouldBeEmpty()
                result.tests shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleKotlinTest",
                            name = "ExampleKotlinTest",
                            position =
                            Position(
                                filename = "/ExampleKotlinTest.kt",
                                startLine = 6,
                                endLine = 11,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleKotlinTest::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleKotlinTest.kt",
                                        startLine = 8,
                                        endLine = 10,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("single test - backtick escaped name") {
                val ktFile =
                    createKtFile(
                        "ExampleKotlinTest.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import kotlin.test.Test
                        
                        class ExampleKotlinTest {
                            @Test
                            fun `custom name`() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val result = JUnitTestDiscoverer.discoverTests(ktFile)

                result.warnings.shouldBeEmpty()
                result.tests shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleKotlinTest",
                            name = "ExampleKotlinTest",
                            position =
                            Position(
                                filename = "/ExampleKotlinTest.kt",
                                startLine = 6,
                                endLine = 11,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleKotlinTest::custom name",
                                    name = "custom name",
                                    position =
                                    Position(
                                        filename = "/ExampleKotlinTest.kt",
                                        startLine = 8,
                                        endLine = 10,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("single test - ignore") {
                val ktFile =
                    createKtFile(
                        "ExampleKotlinTest.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import kotlin.test.Ignore
                        import kotlin.test.Test
                        
                        class ExampleKotlinTest {
                            @Test
                            @Ignore
                            fun test() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val result = JUnitTestDiscoverer.discoverTests(ktFile)

                result.warnings.shouldBeEmpty()
                result.tests.shouldBeEmpty()
            }

            test("multiple tests") {
                val ktFile =
                    createKtFile(
                        "ExampleKotlinTest.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import kotlin.test.Test
                        
                        class ExampleKotlinTest {
                            @Test
                            fun test() {
                                assertEquals(1, 1)
                            }

                            @Test
                            fun test1() {
                                assertEquals(1, 1)
                            }

                            @Test
                            fun test2() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val result = JUnitTestDiscoverer.discoverTests(ktFile)

                result.warnings.shouldBeEmpty()
                result.tests shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleKotlinTest",
                            name = "ExampleKotlinTest",
                            position =
                            Position(
                                filename = "/ExampleKotlinTest.kt",
                                startLine = 6,
                                endLine = 21,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleKotlinTest::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleKotlinTest.kt",
                                        startLine = 8,
                                        endLine = 10,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleKotlinTest::test1",
                                    name = "test1",
                                    position =
                                    Position(
                                        filename = "/ExampleKotlinTest.kt",
                                        startLine = 13,
                                        endLine = 15,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleKotlinTest::test2",
                                    name = "test2",
                                    position =
                                    Position(
                                        filename = "/ExampleKotlinTest.kt",
                                        startLine = 18,
                                        endLine = 20,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                            ),
                        ),
                    )
            }
        }
    })
