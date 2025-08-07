package io.github.codymikol.kotlintestlauncher.junit

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

open class Example {
    @Test
    fun pass() {
        assertEquals(1, 1)
    }
}

class Subclass : Example()

class JUnitTestRunnerSpec : FunSpec ({
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