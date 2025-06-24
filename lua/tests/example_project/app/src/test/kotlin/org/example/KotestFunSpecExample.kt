package org.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class KotestFunSpecExample : FunSpec({
    context("namespace") {
        test("pass") {
            "a" shouldBe "a"
        }

        test("fail") {
            "a" shouldBe "b"
        }

        context("nested namespace") {
            test("pass") {
                "a" shouldBe "a"
            }

            test("fail") {
                "a" shouldBe "b"
            }
        }
    }
})
