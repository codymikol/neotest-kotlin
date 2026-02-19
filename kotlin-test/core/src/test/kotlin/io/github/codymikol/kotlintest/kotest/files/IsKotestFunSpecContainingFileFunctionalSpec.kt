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

class IsKotestFunSpecContainingFileFunctionalSpec : FunSpec({

    val isTestContainingFileExecutor = IsTestContainingFileExecutor()

    val kotestSubclassAnalysis = kotestSubclassAnalysis()

    context("When a file doesn't contain a FunSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleFunSpec.kt",

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

    context("When a file contains a FunSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleFunSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.FunSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFunSpec : FunSpec({
                            context("f:namespace") {
                                test("test") {
                                  1 shouldBe 1
                                }
                            }
                        })
                """.trimIndent(),
            )

        val result = isTestContainingFileExecutor.getTestFileResult(ktFile)

        test("that the result is a test file") {
            result.isTestFile().shouldBeTrue()
        }

        test("The correct file types are reported") {
            result.types shouldBe listOf(TestFileType.KotestFunSpec)
        }
    }

    context("When a file contains a subclassed FunSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleFunSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.FunSpec
                        import io.kotest.core.spec.style.FunSpecSubclass
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleFunSpec : FunSpecSubclass({
                            context("f:namespace") {
                                test("test") {
                                  1 shouldBe 1
                                }
                            }
                        })
                """.trimIndent(),
                dependencies = listOf(kotestSubclassAnalysis) as Collection<Analysis>
            )

        val result = isTestContainingFileExecutor.getTestFileResult(ktFile)

        test("that the result is a test file") {
            result.isTestFile().shouldBeTrue()
        }

        test("The correct file types are reported") {
            result.types shouldBe listOf(TestFileType.KotestFunSpec)
        }
    }
})
