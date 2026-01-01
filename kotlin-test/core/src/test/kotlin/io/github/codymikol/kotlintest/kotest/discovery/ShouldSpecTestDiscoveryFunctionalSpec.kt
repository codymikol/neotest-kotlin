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
                                start = 6,
                                end = 10,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleShouldSpec::should",
                                    name = "should",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        start = 7,
                                        end = 9,
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
                                start = 6,
                                end = 12,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleShouldSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        start = 7,
                                        end = 11,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleShouldSpec::container::should",
                                            name = "should",
                                            position =
                                            Position(
                                                filename = "/ExampleShouldSpec.kt",
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
                                start = 6,
                                end = 16,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleShouldSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        start = 7,
                                        end = 15,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleShouldSpec::container1::container2",
                                            name = "container2",
                                            position =
                                            Position(
                                                filename = "/ExampleShouldSpec.kt",
                                                start = 8,
                                                end = 14,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleShouldSpec::container1::container2::container3",
                                                    name = "container3",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleShouldSpec.kt",
                                                        start = 9,
                                                        end = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleShouldSpec::container1::container2::container3::should",
                                                            name = "should",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleShouldSpec.kt",
                                                                start = 10,
                                                                end = 12,
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
                                start = 6,
                                end = 18,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleShouldSpec::should",
                                    name = "should",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        start = 7,
                                        end = 9,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleShouldSpec::should1",
                                    name = "should1",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        start = 11,
                                        end = 13,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleShouldSpec::should2",
                                    name = "should2",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        start = 15,
                                        end = 17,
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
                                start = 6,
                                end = 28,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleShouldSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleShouldSpec.kt",
                                        start = 7,
                                        end = 11,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleShouldSpec::container1::should1",
                                            name = "should1",
                                            position =
                                            Position(
                                                filename = "/ExampleShouldSpec.kt",
                                                start = 8,
                                                end = 10,
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
                                        start = 13,
                                        end = 23,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleShouldSpec::container2::container3",
                                            name = "container3",
                                            position =
                                            Position(
                                                filename = "/ExampleShouldSpec.kt",
                                                start = 14,
                                                end = 18,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Test(
                                                    id = "org.example.ExampleShouldSpec::container2::container3::should2",
                                                    name = "should2",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleShouldSpec.kt",
                                                        start = 15,
                                                        end = 17,
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
                                                start = 20,
                                                end = 22,
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
                                        start = 25,
                                        end = 27,
                                    ),
                                ),
                            ),
                        ),
                    )
            }
        }
    })
