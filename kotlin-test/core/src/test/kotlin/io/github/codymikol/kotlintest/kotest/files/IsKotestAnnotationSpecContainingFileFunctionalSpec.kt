package io.github.codymikol.kotlintest.kotest.files

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.files.isTestFile
import io.github.codymikol.kotlintest.kotest.kotestSubclassAnalysis
import io.github.codymikol.kotlintest.provider.Analysis
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.intellij.lang.annotations.Language

class IsKotestAnnotationSpecContainingFileFunctionalSpec : FunSpec({

    val kotestSubclassAnalysis = kotestSubclassAnalysis()

    context("When a file doesn't contain an AnnotationSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleAnnotationSpec.kt",

                """
                        package org.example
                                
                        class Foo {
                          // big mood
                        }
                """.trimIndent(),
            )

        val result = ktFile.isTestFile()

        test("that the result is NOT a test file") {
            result.shouldBeFalse()
        }
    }

    context("When a file contains an AnnotationSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleAnnotationSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.AnnotationSpec
                                                
                        class ExampleAnnotationSpec : AnnotationSpec() {
                            @Test
                            fun example() {
                                1 shouldBe 1
                            }
                        }
                """.trimIndent(),
            )

        val result = ktFile.isTestFile()

        test("that the result is a test file") {
            result.shouldBeTrue()
        }
    }

    context("When a file contains a subclassed AnnotationSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleAnnotationSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.AnnotationSpec
                        import io.kotest.core.spec.style.AnnotationSpecSubclass
                                                
                        class ExampleAnnotationSpec : AnnotationSpecSubclass() {
                            @Test
                            fun example() {
                                1 shouldBe 1
                            }
                        }
                """.trimIndent(),
                dependencies = listOf(kotestSubclassAnalysis) as Collection<Analysis>
            )

        val result = ktFile.isTestFile()

        test("that the result is a test file") {
            result.shouldBeTrue()
        }
    }
})
