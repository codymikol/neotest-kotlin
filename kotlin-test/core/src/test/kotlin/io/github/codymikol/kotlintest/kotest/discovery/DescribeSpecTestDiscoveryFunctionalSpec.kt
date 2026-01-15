package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.Position
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class DescribeSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("DescribeSpec Discovery") {
            test("init block") {
                val ktFile =
                    createKtFile(
                        "ExampleDescribeSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.DescribeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleDescribeSpec : DescribeSpec() {
                            init {
                                it("test") {
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
                            id = "org.example.ExampleDescribeSpec",
                            name = "ExampleDescribeSpec",
                            position =
                            Position(
                                filename = "/ExampleDescribeSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleDescribeSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleDescribeSpec.kt",
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
                        "ExampleDescribeSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.DescribeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleDescribeSpec : DescribeSpec({
                            it("test") {
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
                            id = "org.example.ExampleDescribeSpec",
                            name = "ExampleDescribeSpec",
                            position =
                            Position(
                                filename = "/ExampleDescribeSpec.kt",
                                startColumn = 1,
                                endColumn = 1,
                                startLine = 6,
                                endLine = 10,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleDescribeSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleDescribeSpec.kt",
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
                        "ExampleDescribeSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.DescribeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleDescribeSpec : DescribeSpec({
                            describe("container") {
                                it("test") {
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
                            id = "org.example.ExampleDescribeSpec",
                            name = "ExampleDescribeSpec",
                            position =
                            Position(
                                filename = "/ExampleDescribeSpec.kt",
                                startLine = 6,
                                endLine = 12,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleDescribeSpec::container",
                                    name = "container",
                                    position =
                                    Position(
                                        filename = "/ExampleDescribeSpec.kt",
                                        startLine = 7,
                                        endLine = 11,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleDescribeSpec::container::test",
                                            name = "test",
                                            position =
                                            Position(
                                                filename = "/ExampleDescribeSpec.kt",
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
                        "ExampleDescribeSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.DescribeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleDescribeSpec : DescribeSpec({
                            describe("container1") {
                                describe("container2") {
                                    describe("container3") {
                                        it("test") {
                                          1 shouldBe 1
                                        }
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
                            id = "org.example.ExampleDescribeSpec",
                            name = "ExampleDescribeSpec",
                            position =
                            Position(
                                filename = "/ExampleDescribeSpec.kt",
                                startLine = 6,
                                endLine = 16,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleDescribeSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleDescribeSpec.kt",
                                        startLine = 7,
                                        endLine = 15,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleDescribeSpec::container1::container2",
                                            name = "container2",
                                            position =
                                            Position(
                                                filename = "/ExampleDescribeSpec.kt",
                                                startLine = 8,
                                                endLine = 14,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Container(
                                                    id = "org.example.ExampleDescribeSpec::container1::container2::container3",
                                                    name = "container3",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleDescribeSpec.kt",
                                                        startLine = 9,
                                                        endLine = 13,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                    tests =
                                                    setOf(
                                                        Discovered.Test(
                                                            id = "org.example.ExampleDescribeSpec::container1::container2::container3::test",
                                                            name = "test",
                                                            position =
                                                            Position(
                                                                filename = "/ExampleDescribeSpec.kt",
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
                        "ExampleDescribeSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.DescribeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleDescribeSpec : DescribeSpec({
                            it("test") {
                              1 shouldBe 1
                            }
                            
                            it("test1") {
                              1 shouldBe 1
                            }
                            
                            it("test2") {
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
                            id = "org.example.ExampleDescribeSpec",
                            name = "ExampleDescribeSpec",
                            position =
                            Position(
                                filename = "/ExampleDescribeSpec.kt",
                                startLine = 6,
                                endLine = 18,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleDescribeSpec::test",
                                    name = "test",
                                    position =
                                    Position(
                                        filename = "/ExampleDescribeSpec.kt",
                                        startLine = 7,
                                        endLine = 9,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleDescribeSpec::test1",
                                    name = "test1",
                                    position =
                                    Position(
                                        filename = "/ExampleDescribeSpec.kt",
                                        startLine = 11,
                                        endLine = 13,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleDescribeSpec::test2",
                                    name = "test2",
                                    position =
                                    Position(
                                        filename = "/ExampleDescribeSpec.kt",
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
                        "ExampleDescribeSpec.kt",
                        """
                        package org.example

                        import io.kotest.core.spec.style.DescribeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleDescribeSpec : DescribeSpec({
                            describe("container1") {
                                it("test1") {
                                    1 shouldBe 1
                                }
                            }
                            
                            describe("container2") {
                                describe("container3") {
                                    it("test2") {
                                        1 shouldBe 1
                                    }
                                }
                                
                                it("test3") {
                                    1 shouldBe 1
                                }
                            }
                            
                            it("test4") {
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
                            id = "org.example.ExampleDescribeSpec",
                            name = "ExampleDescribeSpec",
                            position =
                            Position(
                                filename = "/ExampleDescribeSpec.kt",
                                startLine = 6,
                                endLine = 28,
                                startColumn = 1,
                                endColumn = 1,
                            ),
                            tests =
                            setOf(
                                Discovered.Container(
                                    id = "org.example.ExampleDescribeSpec::container1",
                                    name = "container1",
                                    position =
                                    Position(
                                        filename = "/ExampleDescribeSpec.kt",
                                        startLine = 7,
                                        endLine = 11,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Test(
                                            id = "org.example.ExampleDescribeSpec::container1::test1",
                                            name = "test1",
                                            position =
                                            Position(
                                                filename = "/ExampleDescribeSpec.kt",
                                                startLine = 8,
                                                endLine = 10,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                        ),
                                    ),
                                ),
                                Discovered.Container(
                                    id = "org.example.ExampleDescribeSpec::container2",
                                    name = "container2",
                                    position =
                                    Position(
                                        filename = "/ExampleDescribeSpec.kt",
                                        startLine = 13,
                                        endLine = 23,
                                        startColumn = 5,
                                        endColumn = 5,
                                    ),
                                    tests =
                                    setOf(
                                        Discovered.Container(
                                            id = "org.example.ExampleDescribeSpec::container2::container3",
                                            name = "container3",
                                            position =
                                            Position(
                                                filename = "/ExampleDescribeSpec.kt",
                                                startLine = 14,
                                                endLine = 18,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                            tests =
                                            setOf(
                                                Discovered.Test(
                                                    id = "org.example.ExampleDescribeSpec::container2::container3::test2",
                                                    name = "test2",
                                                    position =
                                                    Position(
                                                        filename = "/ExampleDescribeSpec.kt",
                                                        startLine = 15,
                                                        endLine = 17,
                                                        startColumn = 13,
                                                        endColumn = 13,
                                                    ),
                                                ),
                                            ),
                                        ),
                                        Discovered.Test(
                                            id = "org.example.ExampleDescribeSpec::container2::test3",
                                            name = "test3",
                                            position =
                                            Position(
                                                filename = "/ExampleDescribeSpec.kt",
                                                startLine = 20,
                                                endLine = 22,
                                                startColumn = 9,
                                                endColumn = 9,
                                            ),
                                        ),
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleDescribeSpec::test4",
                                    name = "test4",
                                    position =
                                    Position(
                                        filename = "/ExampleDescribeSpec.kt",
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
