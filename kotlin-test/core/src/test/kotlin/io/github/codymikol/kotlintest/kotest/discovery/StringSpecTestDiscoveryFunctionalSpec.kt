package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.Position
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class StringSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("StringSpec Discovery") {
            test("init block") {
                val ktFile =
                    createKtFile(
                        "ExampleStringSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.StringSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleStringSpec : StringSpec() {
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
                            id = "org.example.ExampleStringSpec",
                            name = "ExampleStringSpec",
                            position =
                            Position(
                                filename = "/ExampleStringSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleStringSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleStringSpec.kt",
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
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleStringSpec",
                            name = "ExampleStringSpec",
                            position =
                            Position(
                                filename = "/ExampleStringSpec.kt",
                                startLine = 6,
                                endLine = 10,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleStringSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleStringSpec.kt",
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

            test("multiple top-level test") {
                val ktFile =
                    createKtFile(
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
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleStringSpec",
                            name = "ExampleStringSpec",
                            position =
                            Position(
                                filename = "/ExampleStringSpec.kt",
                                startLine = 6,
                                endLine = 14,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleStringSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleStringSpec.kt",
                                        startLine = 7,
                                        endLine = 9,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleStringSpec::other example string",
                                    name = "other example string",
                                    position =
                                    Position(
                                        filename = "/ExampleStringSpec.kt",
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
        }
    })
