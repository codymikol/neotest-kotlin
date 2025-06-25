package org.example

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class KotestShouldSpecExample : ShouldSpec({
    context("namespace") {
        should("pass") {
            "a" shouldBe "a"
        }

        should("fail") {
            "a" shouldBe "b"
        }

        context("nested namespace") {
            should("pass") {
                "a" shouldBe "a"
            }

            should("fail") {
                "a" shouldBe "b"
            }
        }
    }
})
