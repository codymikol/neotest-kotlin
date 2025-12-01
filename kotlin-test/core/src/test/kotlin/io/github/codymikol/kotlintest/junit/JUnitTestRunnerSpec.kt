package io.github.codymikol.kotlintest.junit

import io.github.codymikol.kotlintest.execute.junit.JUnitTestRunner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JUnitTestRunnerSpec :
    FunSpec({
        context("isRunnable") {
            test("@Test method class") {
                JUnitTestRunner.isRunnable(Example::class) shouldBe true
            }

            test("Example subclass without methods") {
                JUnitTestRunner.isRunnable(Subclass::class) shouldBe true
            }

            test("fail") {
                JUnitTestRunner.isRunnable(String::class) shouldBe false
            }
        }
    })

open class Example {
    @Test
    fun pass() {
        assertEquals(1, 1)
    }
}

class Subclass : Example()
