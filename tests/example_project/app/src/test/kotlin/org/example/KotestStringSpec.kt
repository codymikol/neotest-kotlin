package org.example
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class KotestStringSpec : StringSpec({
    "pass" {
        1 shouldBe 1
    }

    "fail" {
        1 shouldBe 2
    }
})
