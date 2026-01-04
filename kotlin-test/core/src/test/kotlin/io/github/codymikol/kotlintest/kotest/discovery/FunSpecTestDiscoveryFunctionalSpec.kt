package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class FunSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("FunSpec Discovery") {
            test("init block") {
                val ktFile =
                    createKtFile(
                        "ExampleFunSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.FunSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFunSpec : FunSpec() {
                            init {
                                test("test") {
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
                            id = "org.example.ExampleFunSpec",
                            name = "ExampleFunSpec",
                            position =
                            Position(
                                filename = "/ExampleFunSpec.kt",
                                start = 6,
                                end = 12,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleFunSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleFunSpec.kt",
                                        start = 8,
                                        end = 10,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("top-level test") {
                val ktFile =
                    createKtFile(
                        "ExampleFunSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.FunSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFunSpec : FunSpec({
                            test("test") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFunSpec",
                            name = "ExampleFunSpec",
                            position =
                            Position(
                                filename = "/ExampleFunSpec.kt",
                                start = 6,
                                end = 10,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleFunSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleFunSpec.kt",
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
                        "ExampleFunSpec.kt",
                        """
                        package org.example
                        
                        import io.kotest.core.spec.style.FunSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFunSpec : FunSpec({
                            context("container") {
                                test("test") {
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
                            id = "org.example.ExampleFunSpec",
                            name = "ExampleFunSpec",
                            position =
                            Position(
                                filename = "/ExampleFunSpec.kt",
                                start = 6,
                                end = 12,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleFunSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleFunSpec.kt",
                                        start = 7,
                                        end = 11,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleFunSpec::container::test",
                                            name = "test",
                                            position =
                                            Position(
                                                filename = "/ExampleFunSpec.kt",
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
                        "ExampleFunSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.FunSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFunSpec : FunSpec({
                            context("container1") {
                                context("container2") {
                                    context("container3") {
                                        test("test") {
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
                            id = "org.example.ExampleFunSpec",
                            name = "ExampleFunSpec",
                            position =
                            Position(
                                filename = "/ExampleFunSpec.kt",
                                start = 6,
                                end = 16,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleFunSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleFunSpec.kt",
                                        start = 7,
                                        end = 15,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleFunSpec::container1::container2",
                                            name = "container2",
                                            position =
                                            Position(
                                                filename = "/ExampleFunSpec.kt",
                                                start = 8,
                                                end = 14,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleFunSpec::container1::container2::container3",
                                                    name = "container3",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleFunSpec.kt",
                                                        start = 9,
                                                        end = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleFunSpec::container1::container2::container3::test",
                                                            name = "test",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleFunSpec.kt",
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
                        "ExampleFunSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.FunSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFunSpec : FunSpec({
                            test("test") {
                              1 shouldBe 1
                            }
                            
                            test("test1") {
                              1 shouldBe 1
                            }
                            
                            test("test2") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFunSpec",
                            name = "ExampleFunSpec",
                            position =
                            Position(
                                filename = "/ExampleFunSpec.kt",
                                start = 6,
                                end = 18,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleFunSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleFunSpec.kt",
                                        start = 7,
                                        end = 9,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleFunSpec::test1",
                                    name = "test1",
                                    position =
                                    Position(
                                        filename = "/ExampleFunSpec.kt",
                                        start = 11,
                                        end = 13,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleFunSpec::test2",
                                    name = "test2",
                                    position =
                                    Position(
                                        filename = "/ExampleFunSpec.kt",
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
                        "ExampleFunSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.FunSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFunSpec : FunSpec({
                            context("container1") {
                                test("test1") {
                                    1 shouldBe 1
                                }
                            }
                            
                            context("container2") {
                                context("container3") {
                                    test("test2") {
                                        1 shouldBe 1
                                    }
                                }
                                
                                test("test3") {
                                    1 shouldBe 1
                                }
                            }
                            
                            test("test4") {
                              1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleFunSpec",
                            name = "ExampleFunSpec",
                            position =
                            Position(
                                filename = "/ExampleFunSpec.kt",
                                start = 6,
                                end = 28,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleFunSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleFunSpec.kt",
                                        start = 7,
                                        end = 11,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleFunSpec::container1::test1",
                                            name = "test1",
                                            position =
                                            Position(
                                                filename = "/ExampleFunSpec.kt",
                                                start = 8,
                                                end = 10,
                                            ),
                                        ),
                                    ),
                                ),
                                Discovered.Container(
                                    id = "org.example.ExampleFunSpec::container2",
                                    name = "container2",
                                    position =
                                    Position(
                                        filename = "/ExampleFunSpec.kt",
                                        start = 13,
                                        end = 23,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleFunSpec::container2::container3",
                                            name = "container3",
                                            position =
                                            Position(
                                                filename = "/ExampleFunSpec.kt",
                                                start = 14,
                                                end = 18,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Test(
                                                    id = "org.example.ExampleFunSpec::container2::container3::test2",
                                                    name = "test2",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleFunSpec.kt",
                                                        start = 15,
                                                        end = 17,
                                                    ),
                                                ),
                                            ),
                                        ),
                                        Discovered.Test(
                                            id = "org.example.ExampleFunSpec::container2::test3",
                                            name = "test3",
                                            position =
                                            Position(
                                                filename = "/ExampleFunSpec.kt",
                                                start = 20,
                                                end = 22,
                                            ),
                                        ),
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleFunSpec::test4",
                                    name = "test4",
                                    position =
                                    Position(
                                        filename = "/ExampleFunSpec.kt",
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
