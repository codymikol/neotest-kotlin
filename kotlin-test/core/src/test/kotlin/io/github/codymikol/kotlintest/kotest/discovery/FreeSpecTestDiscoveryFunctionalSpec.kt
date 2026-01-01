package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class FreeSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("FreeSpec Discovery") {
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

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFreeSpec",
                            name = "ExampleFreeSpec",
                            position =
                            Position(
                                filename = "/ExampleFreeSpec.kt",
                                start = 6,
                                end = 10,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleFreeSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleFreeSpec.kt",
                                        start = 7,
                                        end = 9,
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

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFreeSpec",
                            name = "ExampleFreeSpec",
                            position =
                            Position(
                                filename = "/ExampleFreeSpec.kt",
                                start = 6,
                                end = 12,
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

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFreeSpec",
                            name = "ExampleFreeSpec",
                            position =
                            Position(
                                filename = "/ExampleFreeSpec.kt",
                                start = 6,
                                end = 12,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleFreeSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleFreeSpec.kt",
                                        start = 7,
                                        end = 11,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleFreeSpec::container::example string",
                                            name = "example string",
                                            position =
                                            Position(
                                                filename = "/ExampleFreeSpec.kt",
                                                start = 8,
                                                end = 10,
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

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFreeSpec",
                            name = "ExampleFreeSpec",
                            position =
                            Position(
                                filename = "/ExampleFreeSpec.kt",
                                start = 6,
                                end = 14,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleFreeSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleFreeSpec.kt",
                                        start = 7,
                                        end = 13,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleFreeSpec::container::container1",
                                            name = "container1",
                                            position =
                                            Position(
                                                filename = "/ExampleFreeSpec.kt",
                                                start = 8,
                                                end = 12,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Test(
                                                    id = "org.example.ExampleFreeSpec::container::container1::example string",
                                                    name = "example string",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleFreeSpec.kt",
                                                        start = 9,
                                                        end = 11,
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
