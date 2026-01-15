package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.Position
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class FreeSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("FreeSpec Discovery") {
            test("init block") {
                val ktFile =
                    createKtFile(
                        "ExampleFreeSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.FreeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFreeSpec : FreeSpec() {
                            init {
                                "example string" {
                                    1 shouldBe 1
                                }
                            }
                        }
                        """.trimIndent(),
                    )

                val result = KotestTestDiscoverer.discoverTests(ktFile)

                result.warnings.shouldBeEmpty()
                result.tests shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFreeSpec",
                            name = "ExampleFreeSpec",
                            position =
                            Position(
                                filename = "/ExampleFreeSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleFreeSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleFreeSpec.kt",
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
                        "ExampleFreeSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.FreeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFreeSpec : FreeSpec({
                            "example string" {
                                1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val result = KotestTestDiscoverer.discoverTests(ktFile)

                result.warnings.shouldBeEmpty()
                result.tests shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFreeSpec",
                            name = "ExampleFreeSpec",
                            position =
                            Position(
                                filename = "/ExampleFreeSpec.kt",
                                startLine = 6,
                                endLine = 10,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleFreeSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleFreeSpec.kt",
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
                        "ExampleFreeSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.FreeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFreeSpec : FreeSpec({
                            "container" should {
                                "example string" {
                                    1 shouldBe 1
                                }
                            }
                        })
                        """.trimIndent(),
                    )

                val result = KotestTestDiscoverer.discoverTests(ktFile)

                result.warnings.shouldBeEmpty()
                result.tests shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFreeSpec",
                            name = "ExampleFreeSpec",
                            position =
                            Position(
                                filename = "/ExampleFreeSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests = emptySet(),
                        ),
                    )
            }

            test("nested test") {
                val ktFile =
                    createKtFile(
                        "ExampleFreeSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.FreeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFreeSpec : FreeSpec({
                            "container" - {
                                "example string" {
                                    1 shouldBe 1
                                }
                            }
                        })
                        """.trimIndent(),
                    )

                val result = KotestTestDiscoverer.discoverTests(ktFile)

                result.warnings.shouldBeEmpty()
                result.tests shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFreeSpec",
                            name = "ExampleFreeSpec",
                            position =
                            Position(
                                filename = "/ExampleFreeSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleFreeSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleFreeSpec.kt",
                                        startLine = 7,
                                        endLine = 11,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleFreeSpec::container::example string",
                                            name = "example string",
                                            position =
                                            Position(
                                                filename = "/ExampleFreeSpec.kt",
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
                        "ExampleFreeSpec.kt",
                        """
                        package org.example

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
                        """.trimIndent(),
                    )

                val result = KotestTestDiscoverer.discoverTests(ktFile)

                result.warnings.shouldBeEmpty()
                result.tests shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFreeSpec",
                            name = "ExampleFreeSpec",
                            position =
                            Position(
                                filename = "/ExampleFreeSpec.kt",
                                startLine = 6,
                                endLine = 14,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleFreeSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleFreeSpec.kt",
                                        startLine = 7,
                                        endLine = 13,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleFreeSpec::container::container1",
                                            name = "container1",
                                            position =
                                            Position(
                                                filename = "/ExampleFreeSpec.kt",
                                                startLine = 8,
                                                endLine = 12,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Test(
                                                    id = "org.example.ExampleFreeSpec::container::container1::example string",
                                                    name = "example string",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleFreeSpec.kt",
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
