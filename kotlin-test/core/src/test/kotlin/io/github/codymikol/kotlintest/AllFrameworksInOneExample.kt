package io.github.codymikol.kotlintest

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeOdd
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import kotlin.test.Test as KotlinTest
import org.junit.jupiter.api.Test as JUnitTest

class AllFrameworksInOneExample :
    FunSpec({
        context("namespace") {
            test("pass") {
                1.shouldBeOdd()
            }

            test("fail") {
                2.shouldBeOdd()
            }
        }
    }) {
    @JUnitTest
    fun junitPass() {
        assertEquals(1, 1)
    }

    @JUnitTest
    fun junitFail() {
        assertEquals(1, 2)
    }

    @Nested
    inner class JUnitNamespace {
        @JUnitTest
        fun junitPass() {
            assertEquals(1, 1)
        }

        @JUnitTest
        fun junitFail() {
            assertEquals(1, 2)
        }
    }

    @KotlinTest
    fun kotlinPass() {
        assertEquals(1, 1)
    }

    @KotlinTest
    fun kotlinFail() {
        assertEquals(1, 2)
    }
}
