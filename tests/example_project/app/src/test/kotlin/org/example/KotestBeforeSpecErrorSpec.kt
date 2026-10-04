package org.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class KotestBeforeSpecErrorSpec : FunSpec({
    beforeSpec {
        error("beforeSpec failed")
    }

    context("namespace") {
        test("pass") {
            "a" shouldBe "a"
        }
    }
})
