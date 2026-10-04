package io.github.codymikol.kotlintest.kotest

import io.kotest.core.spec.style.funSpec
import io.kotest.matchers.shouldBe

/**
 * Test factories included by [KotestInheritedExample] and [KotestFactoryDuplicateExample].
 */
fun sharedFactory() = funSpec {
    test("factory test") {
        "a" shouldBe "a"
    }

    context("factory container") {
        test("factory nested") {
            "a" shouldBe "b"
        }
    }
}

val sharedFactoryProperty = funSpec {
    test("factory property test") {
        "a" shouldBe "a"
    }
}
