package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.discover.Discovered
import io.github.codymikol.kotlintest.discover.Position
import io.github.codymikol.kotlintest.discover.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@Suppress("MaxLineLength") // tests ids get long
class AnnotationSpecTestDiscoveryFunctionalSpec :
    FunSpec({
        context("AnnotationSpec Discovery") {
            test("single test") {
                val ktFile =
                    createKtFile(
                        "ExampleAnnotationSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.AnnotationSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleAnnotationSpec : AnnotationSpec() {
                            @Test
                            fun example() {
                                1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleAnnotationSpec",
                            name = "ExampleAnnotationSpec",
                            position =
                            Position(
                                filename = "/ExampleAnnotationSpec.kt",
                                start = 6,
                                end = 11,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleAnnotationSpec::example",
                                    name = "example",
                                    position =
                                    Position(
                                        filename = "/ExampleAnnotationSpec.kt",
                                        start = 7,
                                        end = 10,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("single ignored test") {
                val ktFile =
                    createKtFile(
                        "ExampleAnnotationSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.AnnotationSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleAnnotationSpec : AnnotationSpec() {
                            @Test
                            @Ignore
                            fun example() {
                                1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleAnnotationSpec",
                            name = "ExampleAnnotationSpec",
                            position =
                            Position(
                                filename = "/ExampleAnnotationSpec.kt",
                                start = 6,
                                end = 12,
                            ),
                            tests = emptySet(),
                        ),
                    )
            }

            test("single ignored non-test") {
                val ktFile =
                    createKtFile(
                        "ExampleAnnotationSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.AnnotationSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleAnnotationSpec : AnnotationSpec() {
                            @Ignore
                            fun example() {
                                1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleAnnotationSpec",
                            name = "ExampleAnnotationSpec",
                            position =
                            Position(
                                filename = "/ExampleAnnotationSpec.kt",
                                start = 6,
                                end = 11,
                            ),
                            tests = emptySet(),
                        ),
                    )
            }

            test("no @Test annotated functions") {
                val ktFile =
                    createKtFile(
                        "ExampleAnnotationSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.AnnotationSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleAnnotationSpec : AnnotationSpec() {
                            fun example() {
                                1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleAnnotationSpec",
                            name = "ExampleAnnotationSpec",
                            position =
                            Position(
                                filename = "/ExampleAnnotationSpec.kt",
                                start = 6,
                                end = 10,
                            ),
                            tests = emptySet(),
                        ),
                    )
            }

            test("single backticked test") {
                val ktFile =
                    createKtFile(
                        "ExampleAnnotationSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.AnnotationSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleAnnotationSpec : AnnotationSpec() {
                            @Test
                            fun `fancy name with spaces`() {
                                1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleAnnotationSpec",
                            name = "ExampleAnnotationSpec",
                            position =
                            Position(
                                filename = "/ExampleAnnotationSpec.kt",
                                start = 6,
                                end = 11,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleAnnotationSpec::fancy name with spaces",
                                    name = "fancy name with spaces",
                                    position =
                                    Position(
                                        filename = "/ExampleAnnotationSpec.kt",
                                        start = 7,
                                        end = 10,
                                    ),
                                ),
                            ),
                        ),
                    )
            }

            test("multiple top-level test") {
                val ktFile =
                    createKtFile(
                        "ExampleAnnotationSpec.kt",
                        """
                        package org.example
                                
                        import io.kotest.core.spec.style.AnnotationSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleAnnotationSpec : AnnotationSpec() {
                            @Test
                            fun example() {
                                1 shouldBe 1
                            }
                            
                            @Test
                            fun otherExample() {
                                1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
                    )

                val results = KotestTestDiscoverer.discoverTests(ktFile)

                results shouldBe
                    setOf(
                        Discovered.Container(
                            id = "org.example.ExampleAnnotationSpec",
                            name = "ExampleAnnotationSpec",
                            position =
                            Position(
                                filename = "/ExampleAnnotationSpec.kt",
                                start = 6,
                                end = 16,
                            ),
                            tests =
                            setOf(
                                Discovered.Test(
                                    id = "org.example.ExampleAnnotationSpec::example",
                                    name = "example",
                                    position =
                                    Position(
                                        filename = "/ExampleAnnotationSpec.kt",
                                        start = 7,
                                        end = 10,
                                    ),
                                ),
                                Discovered.Test(
                                    id = "org.example.ExampleAnnotationSpec::otherExample",
                                    name = "otherExample",
                                    position =
                                    Position(
                                        filename = "/ExampleAnnotationSpec.kt",
                                        start = 12,
                                        end = 15,
                                    ),
                                ),
                            ),
                        ),
                    )
            }
        }
    })
