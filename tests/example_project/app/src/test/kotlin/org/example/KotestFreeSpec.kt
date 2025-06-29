package org.example
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class KotestFreeSpec : FreeSpec({
    "namespace" - {
        "pass" {
            1 shouldBe 1
        }

        "fail" {
            1 shouldBe 2
        }

        "nested namespace" - {
            "pass" {
                1 shouldBe 1
            }

            "fail" {
                1 shouldBe 2
            }
        }
    }
})
