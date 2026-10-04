package io.github.codymikol.kotlintest.junit

import io.github.codymikol.kotlintest.execute.junit.JUnitTestExecutor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JUnitTestExecutorSpec :
    FunSpec({
        context("isRunnable") {
            test("@Test method class") {
                JUnitTestExecutor.isRunnable(Example::class) shouldBe true
            }

            test("Example subclass without methods") {
                JUnitTestExecutor.isRunnable(Subclass::class) shouldBe true
            }

            test("fail") {
                JUnitTestExecutor.isRunnable(String::class) shouldBe false
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
