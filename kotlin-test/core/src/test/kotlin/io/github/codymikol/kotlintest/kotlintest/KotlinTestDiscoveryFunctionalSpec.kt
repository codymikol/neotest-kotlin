package io.github.codymikol.kotlintest.kotlintest

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.junit.JUnitTestDiscoverer
import io.kotest.core.spec.style.FunSpec
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

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleKotlinTest",
                            name = "ExampleKotlinTest",
                            position =
                            Position(
                                filename = "/ExampleKotlinTest.kt",
                                start = 6,
                                end = 11,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleKotlinTest::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleKotlinTest.kt",
                                        start = 7,
                                        end = 10,
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

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleKotlinTest",
                            name = "ExampleKotlinTest",
                            position =
                            Position(
                                filename = "/ExampleKotlinTest.kt",
                                start = 6,
                                end = 11,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleKotlinTest::custom name",
                                    name = "custom name",
                                    position =
                                    Position(
                                        filename = "/ExampleKotlinTest.kt",
                                        start = 7,
                                        end = 10,
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

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleKotlinTest",
                            name = "ExampleKotlinTest",
                            position =
                            Position(
                                filename = "/ExampleKotlinTest.kt",
                                start = 7,
                                end = 13,
                            ),
                            tests = emptySet(),
                        ),
                    )
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

                val results = JUnitTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleKotlinTest",
                            name = "ExampleKotlinTest",
                            position =
                            Position(
                                filename = "/ExampleKotlinTest.kt",
                                start = 6,
                                end = 21,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleKotlinTest::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleKotlinTest.kt",
                                        start = 7,
                                        end = 10,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleKotlinTest::test1",
                                    name = "test1",
                                    position =
                                    Position(
                                        filename = "/ExampleKotlinTest.kt",
                                        start = 12,
                                        end = 15,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleKotlinTest::test2",
                                    name = "test2",
                                    position =
                                    Position(
                                        filename = "/ExampleKotlinTest.kt",
                                        start = 17,
                                        end = 20,
                                    ),
                                ),
                            ),
                        ),
                    )
            }
        }
    })
