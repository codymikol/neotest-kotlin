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
                    id = "example",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 5,
                        end = 8
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("single ignored test") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
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

            results shouldBe emptySet()
        }

        test("single ignored non-test") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
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

            results shouldBe emptySet()
        }

        test("no @Test annotated functions") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
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

            results shouldBe emptySet()
        }

        test("single backticked test") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
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
                    id = "fancy name with spaces",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 5,
                        end = 8
                    ),
                    type = TestType.TEST
                )
            )
        }

        test("multiple top-level test") {
            val ktFile = createKtFile(
                "ExampleAnnotationSpec.kt",
                """
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
                    id = "example",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 5,
                        end = 8
                    ),
                    type = TestType.TEST
                ),
                DiscoveredTest(
                    id = "otherExample",
                    position = Position(
                        filename = "/ExampleAnnotationSpec.kt",
                        start = 10,
                        end = 13
                    ),
                    type = TestType.TEST
                )
            )
        }
    }
})
