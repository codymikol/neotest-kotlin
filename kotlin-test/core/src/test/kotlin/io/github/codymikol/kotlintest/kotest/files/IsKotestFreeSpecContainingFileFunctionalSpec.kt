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
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language

class IsKotestFreeSpecContainingFileFunctionalSpec : FunSpec({

    val isTestContainingFileExecutor = IsTestContainingFileExecutor()

    val kotestSubclassAnalysis = kotestSubclassAnalysis()

    context("When a file doesn't contain a FreeSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleFreeSpec.kt",

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

    context("When a file contains a FreeSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleFreeSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.FreeSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFreeSpec : FreeSpec() {
                            init {
                                "example string" {
                                    1 shouldBe 1
                                }
                            }
                        }
                        """.trimIndent(),
            )

        val result = isTestContainingFileExecutor.getTestFileResult(ktFile)

        test("that the result is a test file") {
            result.isTestFile().shouldBeTrue()
        }

        test("The correct file types are reported") {
            result.types shouldBe listOf(TestFileType.KotestFreeSpec)
        }
    }

    context("When a file contains a subclassed FreeSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleFreeSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.FreeSpec
                        import io.kotest.core.spec.style.FreeSpecSubclass
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFreeSpec : FreeSpecSubclass() {
                            init {
                                "example string" {
                                    1 shouldBe 1
                                }
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
            result.types shouldBe listOf(TestFileType.KotestFreeSpec)
        }

    }

})