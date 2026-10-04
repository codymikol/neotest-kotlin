package org.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MultipleClassesFirstSpec : FunSpec({
    test("pass") {
        1 shouldBe 1
    }
})

class MultipleClassesSecondSpec : StringSpec({
    "pass" {
        2 shouldBe 2
    }
})
