package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class ShouldSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("ShouldSpec Discovery") {
            test("init block") {
                val ktFile =
                    createKtFile(
                        "ExampleShouldSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ShouldSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleShouldSpec : ShouldSpec() {
                            init {
                                should("should") {
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
                            id = "org.example.ExampleShouldSpec",
                            name = "ExampleShouldSpec",
                            position =
                            Position(
                                filename = "/ExampleShouldSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleShouldSpec::should",
                                    name = "should",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
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

            test("top-level test") {
                val ktFile =
                    createKtFile(
                        "ExampleShouldSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ShouldSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleShouldSpec : ShouldSpec({
                            should("should") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleShouldSpec",
                            name = "ExampleShouldSpec",
                            position =
                            Position(
                                filename = "/ExampleShouldSpec.kt",
                                startLine = 6,
                                endLine = 10,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleShouldSpec::should",
                                    name = "should",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
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

            test("nested test") {
                val ktFile =
                    createKtFile(
                        "ExampleShouldSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ShouldSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleShouldSpec : ShouldSpec({
                            context("container") {
                                should("should") {
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
                            id = "org.example.ExampleShouldSpec",
                            name = "ExampleShouldSpec",
                            position =
                            Position(
                                filename = "/ExampleShouldSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleShouldSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        startLine = 7,
                                        endLine = 11,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleShouldSpec::container::should",
                                            name = "should",
                                            position =
                                            Position(
                                                filename = "/ExampleShouldSpec.kt",
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
                        "ExampleShouldSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ShouldSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleShouldSpec : ShouldSpec({
                            context("container1") {
                                context("container2") {
                                    context("container3") {
                                        should("should") {
                                          1 shouldBe 1
                                        }
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
                            id = "org.example.ExampleShouldSpec",
                            name = "ExampleShouldSpec",
                            position =
                            Position(
                                filename = "/ExampleShouldSpec.kt",
                                startLine = 6,
                                endLine = 16,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleShouldSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        startLine = 7,
                                        endLine = 15,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleShouldSpec::container1::container2",
                                            name = "container2",
                                            position =
                                            Position(
                                                filename = "/ExampleShouldSpec.kt",
                                                startLine = 8,
                                                endLine = 14,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleShouldSpec::container1::container2::container3",
                                                    name = "container3",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleShouldSpec.kt",
                                                        startLine = 9,
                                                        endLine = 13,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleShouldSpec::container1::container2::container3::should",
                                                            name = "should",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleShouldSpec.kt",
                                                                startLine = 10,
                                                                endLine = 12,
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

            test("multiple top-level test") {
                val ktFile =
                    createKtFile(
                        "ExampleShouldSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ShouldSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleShouldSpec : ShouldSpec({
                            should("should") {
                              1 shouldBe 1
                            }
                            
                            should("should1") {
                              1 shouldBe 1
                            }
                            
                            should("should2") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleShouldSpec",
                            name = "ExampleShouldSpec",
                            position =
                            Position(
                                filename = "/ExampleShouldSpec.kt",
                                startLine = 6,
                                endLine = 18,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleShouldSpec::should",
                                    name = "should",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        startLine = 7,
                                        endLine = 9,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleShouldSpec::should1",
                                    name = "should1",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        startLine = 11,
                                        endLine = 13,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleShouldSpec::should2",
                                    name = "should2",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        startLine = 15,
                                        endLine = 17,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("complex test suite") {
                val ktFile =
                    createKtFile(
                        "ExampleShouldSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ShouldSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleShouldSpec : ShouldSpec({
                            context("container1") {
                                should("should1") {
                                    1 shouldBe 1
                                }
                            }
                            
                            context("container2") {
                                context("container3") {
                                    should("should2") {
                                        1 shouldBe 1
                                    }
                                }
                                
                                should("should3") {
                                    1 shouldBe 1
                                }
                            }
                            
                            should("should4") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleShouldSpec",
                            name = "ExampleShouldSpec",
                            position =
                            Position(
                                filename = "/ExampleShouldSpec.kt",
                                startLine = 6,
                                endLine = 28,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleShouldSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        startLine = 7,
                                        endLine = 11,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleShouldSpec::container1::should1",
                                            name = "should1",
                                            position =
                                            Position(
                                                filename = "/ExampleShouldSpec.kt",
                                                startLine = 8,
                                                endLine = 10,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                        ),
                                    ),
                                ),
                                Discovered.Container(
                                    id = "org.example.ExampleShouldSpec::container2",
                                    name = "container2",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        startLine = 13,
                                        endLine = 23,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleShouldSpec::container2::container3",
                                            name = "container3",
                                            position =
                                            Position(
                                                filename = "/ExampleShouldSpec.kt",
                                                startLine = 14,
                                                endLine = 18,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Test(
                                                    id = "org.example.ExampleShouldSpec::container2::container3::should2",
                                                    name = "should2",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleShouldSpec.kt",
                                                        startLine = 15,
                                                        endLine = 17,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                ),
                                            ),
                                        ),
                                        Discovered.Test(
                                            id = "org.example.ExampleShouldSpec::container2::should3",
                                            name = "should3",
                                            position =
                                            Position(
                                                filename = "/ExampleShouldSpec.kt",
                                                startLine = 20,
                                                endLine = 22,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                        ),
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleShouldSpec::should4",
                                    name = "should4",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        startLine = 25,
                                        endLine = 27,
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
