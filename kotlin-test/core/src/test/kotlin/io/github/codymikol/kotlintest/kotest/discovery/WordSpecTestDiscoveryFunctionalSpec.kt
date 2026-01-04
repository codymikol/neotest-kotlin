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
                                start = 6,
                                end = 12,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleWordSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleWordSpec.kt",
                                        start = 8,
                                        end = 10,
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
                                start = 6,
                                end = 10,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleWordSpec::example string",
                                    name = "example string",
                                    position =
                                    Position(
                                        filename = "/ExampleWordSpec.kt",
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
                                start = 6,
                                end = 12,
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
                                start = 6,
                                end = 12,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleWordSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleWordSpec.kt",
                                        start = 7,
                                        end = 11,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleWordSpec::container::example string",
                                            name = "example string",
                                            position =
                                            Position(
                                                filename = "/ExampleWordSpec.kt",
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
                                start = 6,
                                end = 12,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleWordSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleWordSpec.kt",
                                        start = 7,
                                        end = 11,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleWordSpec::container::example string",
                                            name = "example string",
                                            position =
                                            Position(
                                                filename = "/ExampleWordSpec.kt",
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
                                start = 6,
                                end = 14,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleWordSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleWordSpec.kt",
                                        start = 7,
                                        end = 13,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleWordSpec::container::container1",
                                            name = "container1",
                                            position =
                                            Position(
                                                filename = "/ExampleWordSpec.kt",
                                                start = 8,
                                                end = 12,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Test(
                                                    id = "org.example.ExampleWordSpec::container::container1::example string",
                                                    name = "example string",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleWordSpec.kt",
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
