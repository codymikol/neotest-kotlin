package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class FeatureSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("FeatureSpec Discovery") {
            test("init block") {
                val ktFile =
                    createKtFile(
                        "ExampleFeatureSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.FeatureSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFeatureSpec : FeatureSpec() {
                            init {
                                scenario("test") {
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
                            id = "org.example.ExampleFeatureSpec",
                            name = "ExampleFeatureSpec",
                            position =
                            Position(
                                filename = "/ExampleFeatureSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleFeatureSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleFeatureSpec.kt",
                                        startColumn = 9,
                                        endColumn = 9,
                                        startLine = 8,
                                        endLine = 10,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("top-level test") {
                val ktFile =
                    createKtFile(
                        "ExampleFeatureSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.FeatureSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFeatureSpec : FeatureSpec({
                            scenario("test") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFeatureSpec",
                            name = "ExampleFeatureSpec",
                            position =
                            Position(
                                filename = "/ExampleFeatureSpec.kt",
                                startLine = 6,
                                endLine = 10,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleFeatureSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleFeatureSpec.kt",
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
                        "ExampleFeatureSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.FeatureSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFeatureSpec : FeatureSpec({
                            feature("container") {
                                scenario("test") {
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
                            id = "org.example.ExampleFeatureSpec",
                            name = "ExampleFeatureSpec",
                            position =
                            Position(
                                filename = "/ExampleFeatureSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleFeatureSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleFeatureSpec.kt",
                                        startLine = 7,
                                        endLine = 11,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleFeatureSpec::container::test",
                                            name = "test",
                                            position =
                                            Position(
                                                filename = "/ExampleFeatureSpec.kt",
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
                        "ExampleFeatureSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.FeatureSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFeatureSpec : FeatureSpec({
                            feature("container1") {
                                feature("container2") {
                                    feature("container3") {
                                        scenario("test") {
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
                            id = "org.example.ExampleFeatureSpec",
                            name = "ExampleFeatureSpec",
                            position =
                            Position(
                                filename = "/ExampleFeatureSpec.kt",
                                startLine = 6,
                                endLine = 16,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleFeatureSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleFeatureSpec.kt",
                                        startLine = 7,
                                        endLine = 15,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleFeatureSpec::container1::container2",
                                            name = "container2",
                                            position =
                                            Position(
                                                filename = "/ExampleFeatureSpec.kt",
                                                startLine = 8,
                                                endLine = 14,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleFeatureSpec::container1::container2::container3",
                                                    name = "container3",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleFeatureSpec.kt",
                                                        startLine = 9,
                                                        endLine = 13,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleFeatureSpec::container1::container2::container3::test",
                                                            name = "test",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleFeatureSpec.kt",
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
                        "ExampleFeatureSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.FeatureSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFeatureSpec : FeatureSpec({
                            scenario("test") {
                              1 shouldBe 1
                            }
                            
                            scenario("test1") {
                              1 shouldBe 1
                            }
                            
                            scenario("test2") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFeatureSpec",
                            name = "ExampleFeatureSpec",
                            position =
                            Position(
                                filename = "/ExampleFeatureSpec.kt",
                                startLine = 6,
                                endLine = 18,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleFeatureSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleFeatureSpec.kt",
                                        startLine = 7,
                                        endLine = 9,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleFeatureSpec::test1",
                                    name = "test1",
                                    position =
                                    Position(
                                        filename = "/ExampleFeatureSpec.kt",
                                        startLine = 11,
                                        endLine = 13,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleFeatureSpec::test2",
                                    name = "test2",
                                    position =
                                    Position(
                                        filename = "/ExampleFeatureSpec.kt",
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
                        "ExampleFeatureSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.FeatureSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFeatureSpec : FeatureSpec({
                            feature("container1") {
                                scenario("test1") {
                                    1 shouldBe 1
                                }
                            }
                            
                            feature("container2") {
                                feature("container3") {
                                    scenario("test2") {
                                        1 shouldBe 1
                                    }
                                }
                                
                                scenario("test3") {
                                    1 shouldBe 1
                                }
                            }
                            
                            scenario("test4") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFeatureSpec",
                            name = "ExampleFeatureSpec",
                            position =
                            Position(
                                filename = "/ExampleFeatureSpec.kt",
                                startLine = 6,
                                endLine = 28,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleFeatureSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleFeatureSpec.kt",
                                        startLine = 7,
                                        endLine = 11,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleFeatureSpec::container1::test1",
                                            name = "test1",
                                            position =
                                            Position(
                                                filename = "/ExampleFeatureSpec.kt",
                                                startLine = 8,
                                                endLine = 10,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                        ),
                                    ),
                                ),
                                Discovered.Container(
                                    id = "org.example.ExampleFeatureSpec::container2",
                                    name = "container2",
                                    position =
                                    Position(
                                        filename = "/ExampleFeatureSpec.kt",
                                        startLine = 13,
                                        endLine = 23,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleFeatureSpec::container2::container3",
                                            name = "container3",
                                            position =
                                            Position(
                                                filename = "/ExampleFeatureSpec.kt",
                                                startLine = 14,
                                                endLine = 18,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Test(
                                                    id = "org.example.ExampleFeatureSpec::container2::container3::test2",
                                                    name = "test2",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleFeatureSpec.kt",
                                                        startLine = 15,
                                                        endLine = 17,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                ),
                                            ),
                                        ),
                                        Discovered.Test(
                                            id = "org.example.ExampleFeatureSpec::container2::test3",
                                            name = "test3",
                                            position =
                                            Position(
                                                filename = "/ExampleFeatureSpec.kt",
                                                startLine = 20,
                                                endLine = 22,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                        ),
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleFeatureSpec::test4",
                                    name = "test4",
                                    position =
                                    Position(
                                        filename = "/ExampleFeatureSpec.kt",
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
