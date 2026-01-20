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

class IsKotestWordSpecContainingFileFunctionalSpec : FunSpec({

    val isTestContainingFileExecutor = IsTestContainingFileExecutor()

    val kotestSubclassAnalysis = kotestSubclassAnalysis()

    context("When a file doesn't contain a WordSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleWordSpec.kt",

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

    context("When a file contains a WordSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleWordSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.WordSpec
                        import io.kotest.matchers.shouldBe
                        
                        class ExampleWordSpec : WordSpec({
                            "example string" {
                                1 shouldBe 1
                            }
                        })
                        """.trimIndent(),
            )

        val result = isTestContainingFileExecutor.getTestFileResult(ktFile)

        test("that the result is a test file") {
            result.isTestFile().shouldBeTrue()
        }

        test("The correct file types are reported") {
            result.types shouldBe listOf(TestFileType.KotestWordSpec)
        }
    }

    context("When a file contains a subclassed WordSpec test") {

        @Language("kotlin")
        val ktFile =
            createKtFile(
                "ExampleWordSpec.kt",
                """
                        package org.example
                                
                        import io.kotest.core.spec.style.WordSpecSubclass
                        import io.kotest.matchers.shouldBe

                        
                        class ExampleWordSpec : WordSpecSubclass({
                            "example string" {
                                1 shouldBe 1
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
            result.types shouldBe listOf(TestFileType.KotestWordSpec)
        }

    }

})