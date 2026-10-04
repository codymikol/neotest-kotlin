package io.github.codymikol.kotlintest.kotest.files

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.files.isTestFile
import io.github.codymikol.kotlintest.kotest.kotestSubclassAnalysis
import io.github.codymikol.kotlintest.provider.Analysis
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.intellij.lang.annotations.Language

class IsKotestShouldSpecContainingFileFunctionalSpec : FunSpec({

    val kotestSubclassAnalysis = kotestSubclassAnalysis()

    context("When a file doesn't contain a ShouldSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleShouldSpec.kt",

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

    context("When a file contains a ShouldSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleShouldSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.ShouldSpec
                                                
                        class ExampleShouldSpec : ShouldSpec({
                            context("container1") {
                                should("should1") {
                                    1 shouldBe 1
                                }
                            }
                            
                            context("container2") {
                                context("container3") {
                                    should("should2") {
                                        1 shouldBe 1
                                    }
                                }
                                
                                should("should3") {
                                    1 shouldBe 1
                                }
                            }
                            
                            should("should4") {
                              1 shouldBe 1
                            }
                        })
                """.trimIndent(),
            )

        val result = ktFile.isTestFile()

        test("that the result is a test file") {
            result.shouldBeTrue()
        }
    }

    context("When a file contains a subclassed ShouldSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleShouldSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.ShouldSpec
                        import io.kotest.core.spec.style.ShouldSpecSubclass
                                                
                        class ExampleShouldSpec : ShouldSpecSubclass({
                            context("container1") {
                                should("should1") {
                                    1 shouldBe 1
                                }
                            }
                            
                            context("container2") {
                                context("container3") {
                                    should("should2") {
                                        1 shouldBe 1
                                    }
                                }
                                
                                should("should3") {
                                    1 shouldBe 1
                                }
                            }
                            
                            should("should4") {
                              1 shouldBe 1
                            }
                        })
                """.trimIndent(),
                dependencies = listOf(kotestSubclassAnalysis) as Collection<Analysis>
            )

        val result = ktFile.isTestFile()

        test("that the result is a test file") {
            result.shouldBeTrue()
        }
    }
})
