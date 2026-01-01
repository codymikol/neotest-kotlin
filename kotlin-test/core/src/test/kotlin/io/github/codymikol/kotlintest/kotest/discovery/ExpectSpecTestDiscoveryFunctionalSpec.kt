package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class ExpectSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("ExpectSpec Discovery") {
            test("top-level test") {
                val ktFile =
                    createKtFile(
                        "ExampleExpectSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ExpectSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleExpectSpec : ExpectSpec({
                            expect("test") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleExpectSpec",
                            name = "ExampleExpectSpec",
                            position =
                            Position(
                                filename = "/ExampleExpectSpec.kt",
                                start = 6,
                                end = 10,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleExpectSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleExpectSpec.kt",
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
                        "ExampleExpectSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ExpectSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleExpectSpec : ExpectSpec({
                            context("container") {
                                expect("test") {
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
                            id = "org.example.ExampleExpectSpec",
                            name = "ExampleExpectSpec",
                            position =
                            Position(
                                filename = "/ExampleExpectSpec.kt",
                                start = 6,
                                end = 12,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleExpectSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleExpectSpec.kt",
                                        start = 7,
                                        end = 11,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleExpectSpec::container::test",
                                            name = "test",
                                            position =
                                            Position(
                                                filename = "/ExampleExpectSpec.kt",
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
                        "ExampleExpectSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ExpectSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleExpectSpec : ExpectSpec({
                            context("container1") {
                                context("container2") {
                                    context("container3") {
                                        expect("test") {
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
                            id = "org.example.ExampleExpectSpec",
                            name = "ExampleExpectSpec",
                            position =
                            Position(
                                filename = "/ExampleExpectSpec.kt",
                                start = 6,
                                end = 16,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleExpectSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleExpectSpec.kt",
                                        start = 7,
                                        end = 15,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleExpectSpec::container1::container2",
                                            name = "container2",
                                            position =
                                            Position(
                                                filename = "/ExampleExpectSpec.kt",
                                                start = 8,
                                                end = 14,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleExpectSpec::container1::container2::container3",
                                                    name = "container3",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleExpectSpec.kt",
                                                        start = 9,
                                                        end = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleExpectSpec::container1::container2::container3::test",
                                                            name = "test",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleExpectSpec.kt",
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
                        "ExampleExpectSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ExpectSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleExpectSpec : ExpectSpec({
                            expect("test") {
                              1 shouldBe 1
                            }
                            
                            expect("test1") {
                              1 shouldBe 1
                            }
                            
                            expect("test2") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleExpectSpec",
                            name = "ExampleExpectSpec",
                            position =
                            Position(
                                filename = "/ExampleExpectSpec.kt",
                                start = 6,
                                end = 18,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleExpectSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleExpectSpec.kt",
                                        start = 7,
                                        end = 9,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleExpectSpec::test1",
                                    name = "test1",
                                    position =
                                    Position(
                                        filename = "/ExampleExpectSpec.kt",
                                        start = 11,
                                        end = 13,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleExpectSpec::test2",
                                    name = "test2",
                                    position =
                                    Position(
                                        filename = "/ExampleExpectSpec.kt",
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
                        "ExampleExpectSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.ExpectSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleExpectSpec : ExpectSpec({
                            context("container1") {
                                expect("test1") {
                                    1 shouldBe 1
                                }
                            }
                            
                            context("container2") {
                                context("container3") {
                                    expect("test2") {
                                        1 shouldBe 1
                                    }
                                }
                                
                                expect("test3") {
                                    1 shouldBe 1
                                }
                            }
                            
                            expect("test4") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleExpectSpec",
                            name = "ExampleExpectSpec",
                            position =
                            Position(
                                filename = "/ExampleExpectSpec.kt",
                                start = 6,
                                end = 28,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleExpectSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleExpectSpec.kt",
                                        start = 7,
                                        end = 11,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleExpectSpec::container1::test1",
                                            name = "test1",
                                            position =
                                            Position(
                                                filename = "/ExampleExpectSpec.kt",
                                                start = 8,
                                                end = 10,
                                            ),
                                        ),
                                    ),
                                ),
                                Discovered.Container(
                                    id = "org.example.ExampleExpectSpec::container2",
                                    name = "container2",
                                    position =
                                    Position(
                                        filename = "/ExampleExpectSpec.kt",
                                        start = 13,
                                        end = 23,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleExpectSpec::container2::container3",
                                            name = "container3",
                                            position =
                                            Position(
                                                filename = "/ExampleExpectSpec.kt",
                                                start = 14,
                                                end = 18,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Test(
                                                    id = "org.example.ExampleExpectSpec::container2::container3::test2",
                                                    name = "test2",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleExpectSpec.kt",
                                                        start = 15,
                                                        end = 17,
                                                    ),
                                                ),
                                            ),
                                        ),
                                        Discovered.Test(
                                            id = "org.example.ExampleExpectSpec::container2::test3",
                                            name = "test3",
                                            position =
                                            Position(
                                                filename = "/ExampleExpectSpec.kt",
                                                start = 20,
                                                end = 22,
                                            ),
                                        ),
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleExpectSpec::test4",
                                    name = "test4",
                                    position =
                                    Position(
                                        filename = "/ExampleExpectSpec.kt",
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
