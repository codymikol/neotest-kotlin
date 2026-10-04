package io.github.codymikol.kotlintest.kotest.files

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.files.isTestFile
import io.github.codymikol.kotlintest.kotest.kotestSubclassAnalysis
import io.github.codymikol.kotlintest.provider.Analysis
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.intellij.lang.annotations.Language

class IsKotestFeatureSpecContainingFileFunctionalSpec : FunSpec({

    val kotestSubclassAnalysis = kotestSubclassAnalysis()

    context("When a file doesn't contain a FeatureSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleFeatureSpec.kt",

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

    context("When a file contains a FeatureSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleFeatureSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.FeatureSpec
                                                
                        class ExampleFeatureSpec : FeatureSpec() {
                            init {
                                scenario("test") {
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

    context("When a file contains a subclassed FeatureSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleFeatureSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.FeatureSpec
                        import io.kotest.core.spec.style.FeatureSpecSubclass
                                                
                        class ExampleFeatureSpec : FeatureSpecSubclass() {
                            init {
                                scenario("test") {
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
