package io.github.codymikol.kotlintest.junit

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.junit.JUnitTestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.Position
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength", "LargeClass") // tests ids get long
class JUnitDiscoveryFunctionalSpec :
    FunSpec({
        context("JUnit Discovery") {
            test("single test") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Test
                            fun test() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 6,
                                endLine = 11,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleJUnit::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleJUnit.kt",
                                        startColumn = 5,
                                        endColumn = 5,
                                        startLine = 8,
                                        endLine = 10,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("single test - backtick escaped name") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Test
                            fun `custom name`() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 6,
                                endLine = 11,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleJUnit::custom name",
                                    name = "custom name",
                                    position =
                                    Position(
                                        filename = "/ExampleJUnit.kt",
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

            test("single test - private ignored") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Test
                            private fun test() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 6,
                                endLine = 11,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests = emptySet(),
                        ),
                    )
            }

            test("single test - ignore (JUnit 5)") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Ignore
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Test
                            @Ignore
                            fun test() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 7,
                                endLine = 13,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests = emptySet(),
                        ),
                    )
            }

            test("single test - disabled") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Disabled
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Test
                            @Disabled
                            fun test() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 7,
                                endLine = 13,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests = emptySet(),
                        ),
                    )
            }

            test("test factory") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        $$"""
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.TestFactory
                        import org.junit.jupiter.api.DynamicTest
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @TestFactory
                            fun testSquares() =
                                listOf(
                                    1 to 1,
                                    2 to 4,
                                    3 to 9,
                                    4 to 16,
                                    5 to 25,
                                ).map { (input, expected) ->
                                    DynamicTest.dynamicTest("$input^2 = $expected") {
                                        assertEquals(expected, input * input)
                                    }
                                }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 8,
                                endLine = 22,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleJUnit::testSquares",
                                    name = "testSquares",
                                    position =
                                    Position(
                                        filename = "/ExampleJUnit.kt",
                                        startLine = 10,
                                        endLine = 21,
                                        startColumn = 5,
                                        endColumn = 9,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("parameterized test") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        $$"""
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.ParameterizedTest
                        import org.junit.jupiter.api.ValueSource
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @ParameterizedTest
                            @ValueSource(strings = { "racecar", "radar", "able was I ere I saw elba" })
                            fun palindromes(candidate: String) {
                                assertTrue(StringUtils.isPalindrome(candidate));
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 8,
                                endLine = 14,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleJUnit::palindromes",
                                    name = "palindromes",
                                    position =
                                    Position(
                                        filename = "/ExampleJUnit.kt",
                                        startLine = 11,
                                        endLine = 13,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("class - disabled") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Disabled
                        import org.junit.jupiter.api.Test
                        
                        @Disabled
                        class ExampleJUnit {
                            @Test
                            fun test() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe emptySet()
            }

            test("class - ignore (JUnit 5)") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Ignore
                        import org.junit.jupiter.api.Test
                        
                        @Ignore
                        class ExampleJUnit {
                            @Test
                            fun test() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe emptySet()
            }

            test("class - custom DisplayName") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.DisplayName
                        import org.junit.jupiter.api.Test
                        
                        @DisplayName("custom name")
                        class ExampleJUnit {
                            @Test
                            fun test() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "custom name",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 8,
                                endLine = 13,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleJUnit::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleJUnit.kt",
                                        startLine = 10,
                                        endLine = 12,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("single test - custom DisplayName") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.DisplayName
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Test
                            @DisplayName("custom name")
                            fun test() {
                                assertEquals(1, 1)
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 7,
                                endLine = 13,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleJUnit::custom name",
                                    name = "custom name",
                                    position =
                                    Position(
                                        filename = "/ExampleJUnit.kt",
                                        startLine = 10,
                                        endLine = 12,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("nested test") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Nested
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Nested
                            inner class NestedExampleJUnit {
                                @Test
                                fun test() {
                                    assertEquals(1, 1)
                                }
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 7,
                                endLine = 15,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleJUnit::NestedExampleJUnit",
                                    name = "NestedExampleJUnit",
                                    position =
                                    Position(
                                        filename = "/ExampleJUnit.kt",
                                        startLine = 9,
                                        endLine = 14,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleJUnit::NestedExampleJUnit::test",
                                            name = "test",
                                            position =
                                            Position(
                                                filename = "/ExampleJUnit.kt",
                                                startLine = 11,
                                                endLine = 13,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("nested test - custom DisplayName") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Nested
                        import org.junit.jupiter.api.DisplayName
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Nested
                            @DisplayName("custom name")
                            inner class NestedExampleJUnit {
                                @Test
                                fun test() {
                                    assertEquals(1, 1)
                                }
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startColumn = 1,
                                endColumn = 1,
                                startLine = 8,
                                endLine = 17,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleJUnit::custom name",
                                    name = "custom name",
                                    position =
                                    Position(
                                        filename = "/ExampleJUnit.kt",
                                        startLine = 11,
                                        endLine = 16,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleJUnit::custom name::test",
                                            name = "test",
                                            position =
                                            Position(
                                                filename = "/ExampleJUnit.kt",
                                                startLine = 13,
                                                endLine = 15,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("nested inner class - disabled") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Nested
                        import org.junit.jupiter.api.Disabled
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Disabled
                            @Nested
                            inner class NestedExampleJUnit {
                                @Test
                                fun test() {
                                    assertEquals(1, 1)
                                }
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 8,
                                endLine = 17,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests = emptySet(),
                        ),
                    )
            }

            test("nested inner class - ignore (JUnit 5)") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Nested
                        import org.junit.jupiter.api.Ignore
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Ignore
                            @Nested
                            inner class NestedExampleJUnit {
                                @Test
                                fun test() {
                                    assertEquals(1, 1)
                                }
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 8,
                                endLine = 17,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests = emptySet(),
                        ),
                    )
            }

            test("deeply nested test") {
                val ktFile =
                    createKtFile(
                        "ExampleJUnit.kt",
                        """
                        package org.example

                        import org.junit.jupiter.api.Assertions.assertEquals
                        import org.junit.jupiter.api.Nested
                        import org.junit.jupiter.api.Test
                        
                        class ExampleJUnit {
                            @Nested
                            inner class NestedExampleJUnit {
                                @Nested
                                inner class DoubleNestedExampleJUnit {
                                    @Nested
                                    inner class DeeplyNestedExampleJUnit {
                                        @Test
                                        fun test() {
                                            assertEquals(1, 1)
                                        }
                                    }
                                }
                            }
                        }
                        """.trimIndent(),
                    )

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleJUnit",
                            name = "ExampleJUnit",
                            position =
                            Position(
                                filename = "/ExampleJUnit.kt",
                                startLine = 7,
                                endLine = 21,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleJUnit::NestedExampleJUnit",
                                    name = "NestedExampleJUnit",
                                    position =
                                    Position(
                                        filename = "/ExampleJUnit.kt",
                                        startLine = 9,
                                        endLine = 20,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleJUnit::NestedExampleJUnit::DoubleNestedExampleJUnit",
                                            name = "DoubleNestedExampleJUnit",
                                            position =
                                            Position(
                                                filename = "/ExampleJUnit.kt",
                                                startLine = 11,
                                                endLine = 19,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleJUnit::NestedExampleJUnit::DoubleNestedExampleJUnit::DeeplyNestedExampleJUnit",
                                                    name = "DeeplyNestedExampleJUnit",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleJUnit.kt",
                                                        startLine = 13,
                                                        endLine = 18,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleJUnit::NestedExampleJUnit::DoubleNestedExampleJUnit::DeeplyNestedExampleJUnit::test",
                                                            name = "test",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleJUnit.kt",
                                                                startLine = 15,
                                                                endLine = 17,
                                                                startColumn = 17,
                                                                endColumn = 17,
                                                            ),
                                                        ),
                                                    ),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    )
            }
        }
    })
