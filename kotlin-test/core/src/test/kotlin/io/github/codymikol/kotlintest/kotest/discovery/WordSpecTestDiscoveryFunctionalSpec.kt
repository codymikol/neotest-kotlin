package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class WordSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("WordSpec Discovery") {
            test("init block") {
                val ktFile =
                    createKtFile(
                        "ExampleWordSpec.kt",
                        """
                        package org.example    

                        import io.kotest.core.spec.style.WordSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleWordSpec : WordSpec() {
                            init {
                                "example string" {
                                    1 shouldBe 1
                                }
                            }
                        }
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleWordSpec",
                            name = "ExampleWordSpec",
                            position =
                            Position(
                                filename = "/ExampleWordSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleWordSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleWordSpec.kt",
                                        startLine = 8,
                                        endLine = 10,
                                        startColumn = 9,
                                        endColumn = 9,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("single test") {
                val ktFile =
                    createKtFile(
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
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleWordSpec",
                            name = "ExampleWordSpec",
                            position =
                            Position(
                                filename = "/ExampleWordSpec.kt",
                                startLine = 6,
                                endLine = 10,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleWordSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleWordSpec.kt",
                                        startLine = 7,
                                        endLine = 9,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("invalid container") {
                val ktFile =
                    createKtFile(
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
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleWordSpec",
                            name = "ExampleWordSpec",
                            position =
                            Position(
                                filename = "/ExampleWordSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests = emptySet(),
                        ),
                    )
            }

            test("nested test - `when`") {
                val ktFile =
                    createKtFile(
                        "ExampleWordSpec.kt",
                        """
                        package org.example    

                        import io.kotest.core.spec.style.WordSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleWordSpec : WordSpec({
                            "container" `when` {
                                "example string" {
                                    1 shouldBe 1
                                }
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleWordSpec",
                            name = "ExampleWordSpec",
                            position =
                            Position(
                                filename = "/ExampleWordSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleWordSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleWordSpec.kt",
                                        startLine = 7,
                                        endLine = 11,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleWordSpec::container::example string",
                                            name = "example string",
                                            position =
                                            Position(
                                                filename = "/ExampleWordSpec.kt",
                                                startLine = 8,
                                                endLine = 10,
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

            test("nested test - should") {
                val ktFile =
                    createKtFile(
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
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleWordSpec",
                            name = "ExampleWordSpec",
                            position =
                            Position(
                                filename = "/ExampleWordSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleWordSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleWordSpec.kt",
                                        startLine = 7,
                                        endLine = 11,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleWordSpec::container::example string",
                                            name = "example string",
                                            position =
                                            Position(
                                                filename = "/ExampleWordSpec.kt",
                                                startLine = 8,
                                                endLine = 10,
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

            test("deeply nested test") {
                val ktFile =
                    createKtFile(
                        "ExampleWordSpec.kt",
                        """
                        package org.example    

                        import io.kotest.core.spec.style.WordSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleWordSpec : WordSpec({
                            "container" Should {
                                "container1" When {
                                    "example string" {
                                        1 shouldBe 1
                                    }
                                }
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleWordSpec",
                            name = "ExampleWordSpec",
                            position =
                            Position(
                                filename = "/ExampleWordSpec.kt",
                                startLine = 6,
                                endLine = 14,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleWordSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleWordSpec.kt",
                                        startLine = 7,
                                        endLine = 13,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleWordSpec::container::container1",
                                            name = "container1",
                                            position =
                                            Position(
                                                filename = "/ExampleWordSpec.kt",
                                                startLine = 8,
                                                endLine = 12,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Test(
                                                    id = "org.example.ExampleWordSpec::container::container1::example string",
                                                    name = "example string",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleWordSpec.kt",
                                                        startLine = 9,
                                                        endLine = 11,
                                                        startColumn = 13,
                                                        endColumn = 13,
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
