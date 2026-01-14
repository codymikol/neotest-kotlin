package org.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DuplicateTestNames :
    FunSpec({
        test("pass") {
            "a" shouldBe "a"
        }

        test("pass") {
            "a" shouldBe "b"
        }
    })
