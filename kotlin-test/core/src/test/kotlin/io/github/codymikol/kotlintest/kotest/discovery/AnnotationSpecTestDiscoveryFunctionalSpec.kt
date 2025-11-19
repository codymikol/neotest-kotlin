package io.github.codymikol.kotlintest.kotest.discovery

import io.github.codymikol.kotlintest.DiscoveredTest
import io.github.codymikol.kotlintest.Position
import io.github.codymikol.kotlintest.TestType
import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.kotest.KotestTestDiscoverer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AnnotationSpecTestDiscoveryFunctionalSpec : FunSpec({
    context("AnnotationSpec Discovery") {
        test("single test") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
            package org.example
                    
            import io.kotest.core.spec.style.AnnotationSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleAnnotationSpec : AnnotationSpec({
                @Test
                fun example() {
                    1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleAnnotationSpec",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 6,
                        end = 11
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleAnnotationSpec::example",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 7,
                        end = 10
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("single ignored test") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
            package org.example
                    
            import io.kotest.core.spec.style.AnnotationSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleAnnotationSpec : AnnotationSpec({
                @Test
                @Ignore
                fun example() {
                    1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleAnnotationSpec",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 6,
                        end = 12
                    ),
                    type = TestType.CONTAINER
                ),
            )
        }

        test("single ignored non-test") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
            package org.example
                    
            import io.kotest.core.spec.style.AnnotationSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleAnnotationSpec : AnnotationSpec({
                @Ignore
                fun example() {
                    1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleAnnotationSpec",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 6,
                        end = 11
                    ),
                    type = TestType.CONTAINER
                ),
            )
        }

        test("no @Test annotated functions") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
            package org.example
                    
            import io.kotest.core.spec.style.AnnotationSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleAnnotationSpec : AnnotationSpec({
                fun example() {
                    1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleAnnotationSpec",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 6,
                        end = 10
                    ),
                    type = TestType.CONTAINER
                ),
            )
        }

        test("single backticked test") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
            package org.example
                    
            import io.kotest.core.spec.style.AnnotationSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleAnnotationSpec : AnnotationSpec({
                @Test
                fun `fancy name with spaces`() {
                    1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleAnnotationSpec",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 6,
                        end = 11
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleAnnotationSpec::fancy name with spaces",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 7,
                        end = 10
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("multiple top-level test") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
            package org.example
                    
            import io.kotest.core.spec.style.AnnotationSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleAnnotationSpec : AnnotationSpec({
                @Test
                fun example() {
                    1 shouldBe 1
                }
                
                @Test
                fun otherExample() {
                    1 shouldBe 1
                }
            })
                """.trimIndent()
            )

            val results = KotestTestDiscoverer.discoverTests(ktFile)

            results shouldBe setOf(
                DiscoveredTest(
                    id = "org.example.ExampleAnnotationSpec",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 6,
                        end = 16
                    ),
                    type = TestType.CONTAINER
                ),
                DiscoveredTest(
                    id = "org.example.ExampleAnnotationSpec::example",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 7,
                        end = 10
                    ),
                    type = TestType.TEST
                ),
                DiscoveredTest(
                    id = "org.example.ExampleAnnotationSpec::otherExample",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 12,
                        end = 15
                    ),
                    type = TestType.TEST
                )
            )
        }
    }
})
