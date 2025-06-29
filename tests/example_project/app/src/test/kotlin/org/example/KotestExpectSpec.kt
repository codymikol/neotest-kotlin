package org.example

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe

class KotestExpectSpec : ExpectSpec({
    context("namespace") {
        expect("pass") {
            "a" shouldBe "a"
        }

        expect("fail") {
            "a" shouldBe "b"
        }

        context("nested namespace") {
            expect("pass") {
                "a" shouldBe "a"
            }

            expect("fail") {
                "a" shouldBe "b"
            }
        }
    }
})
