package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class StringSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("StringSpec Discovery") {
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
                                start = 6,
                                end = 10,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleStringSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleStringSpec.kt",
                                        start = 7,
                                        end = 9,
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
                                start = 6,
                                end = 14,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleStringSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleStringSpec.kt",
                                        start = 7,
                                        end = 9,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleStringSpec::other example string",
                                    name = "other example string",
                                    position =
                                    Position(
                                        filename = "/ExampleStringSpec.kt",
                                        start = 11,
                                        end = 13,
                                    ),
                                ),
                            ),
                        ),
                    )
            }
        }
    })
