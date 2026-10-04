package io.github.codymikol.kotlintest.kotest

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Abstract base spec whose tests are run as part of every subclass, see [KotestInheritedExample].
 */
abstract class KotestInheritedBase(body: KotestInheritedBase.() -> Unit) : FunSpec() {
    init {
        test("inherited before body") {
            "a" shouldBe "a"
        }

        body()

        context("inherited container") {
            test("inherited nested") {
                "a" shouldBe "b"
            }
        }
    }
}
