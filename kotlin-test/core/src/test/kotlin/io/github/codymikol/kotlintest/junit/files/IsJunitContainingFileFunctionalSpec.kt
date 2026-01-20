package io.github.codymikol.kotlintest.junit.files

import io.github.codymikol.kotlintest.createKtFile
import io.github.codymikol.kotlintest.files.IsTestContainingFileExecutor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeEmpty
import org.intellij.lang.annotations.Language

class IsJunitContainingFileFunctionalSpec : FunSpec({

    val isTestContainingFileExecutor = IsTestContainingFileExecutor()

    context("When a file doesn't contain a Junit Test") {

        @Language("kotlin")
        val ktFile = createKtFile(
            "ExampleJunitTest.kt",
            """
                package org.example
                            
                class Foo {
                  // big mood
                } 
            """.trimIndent()
        )

        val result = isTestContainingFileExecutor.getTestFileResult(ktFile)

        test("that the result is NOT a test file") {
            result.isTestFile().shouldBeFalse()
        }

        test("that the correct file types are reported") {
            result.types.shouldBeEmpty()
        }

    }

    context("When a file contains a subclassed ")

})