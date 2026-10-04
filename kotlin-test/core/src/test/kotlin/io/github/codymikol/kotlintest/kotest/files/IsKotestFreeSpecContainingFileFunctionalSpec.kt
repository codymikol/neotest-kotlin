package io.github.codymikol.kotlintest.kotest.files

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.files.isTestFile
import io.github.codymikol.kotlintest.kotest.kotestSubclassAnalysis
import io.github.codymikol.kotlintest.provider.Analysis
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.intellij.lang.annotations.Language

class IsKotestFreeSpecContainingFileFunctionalSpec : FunSpec({

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

        val result = ktFile.isTestFile()

        test("that the result is NOT a test file") {
            result.shouldBeFalse()
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
                                                
                        class ExampleFreeSpec : FreeSpec() {
                            init {
                                "example string" {
                                    1 shouldBe 1
                                }
                            }
                        }
                """.trimIndent(),
            )

        val result = ktFile.isTestFile()

        test("that the result is a test file") {
            result.shouldBeTrue()
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

        val result = ktFile.isTestFile()

        test("that the result is a test file") {
            result.shouldBeTrue()
        }
    }
})
