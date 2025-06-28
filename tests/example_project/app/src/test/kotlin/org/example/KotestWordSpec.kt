package org.example
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class KotestWordSpec : WordSpec({
    "first namespace" should {
        "pass" {
            1 shouldBe 1
        }
    }

    "second namespace" `when` {
        "nested namespace" should {
            "pass" {
                1 shouldBe 1
            }

            "fail" {
                1 shouldBe 2
            }
        }
    }

    "third namespace" `when` {
        "nested namespace" should {
            "pass" {
                1 shouldBe 1
            }

            "fail" {
                1 shouldBe 2
            }
        }
    }
})
