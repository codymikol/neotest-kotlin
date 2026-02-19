package io.github.codymikol.kotlintest.kotest.files

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.files.IsTestContainingFileExecutor
import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.kotest.kotestSubclassAnalysis
import io.github.codymikol.kotlintest.provider.Analysis
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language

class IsKotestAnnotationSpecContainingFileFunctionalSpec : FunSpec({

    val isTestContainingFileExecutor = IsTestContainingFileExecutor()

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

        val result = isTestContainingFileExecutor.getTestFileResult(ktFile)

        test("that the result is NOT a test file") {
            result.isTestFile().shouldBeFalse()
        }

        test("The correct file types are reported") {
            result.types.shouldBeEmpty()
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
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleAnnotationSpec : AnnotationSpec() {
                            @Test
                            fun example() {
                                1 shouldBe 1
                            }
                        }
                """.trimIndent(),
            )

        val result = isTestContainingFileExecutor.getTestFileResult(ktFile)

        test("that the result is a test file") {
            result.isTestFile().shouldBeTrue()
        }

        test("The correct file types are reported") {
            result.types shouldBe listOf(TestFileType.KotestAnnotationSpec)
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
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleAnnotationSpec : AnnotationSpecSubclass() {
                            @Test
                            fun example() {
                                1 shouldBe 1
                            }
                        }
                """.trimIndent(),
                dependencies = listOf(kotestSubclassAnalysis) as Collection<Analysis>
            )

        val result = isTestContainingFileExecutor.getTestFileResult(ktFile)

        test("that the result is a test file") {
            result.isTestFile().shouldBeTrue()
        }

        test("The correct file types are reported") {
            result.types shouldBe listOf(TestFileType.KotestAnnotationSpec)
        }
    }
})
