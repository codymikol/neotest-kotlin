package org.example

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class KotestWordSpec :
    WordSpec({
        "When namespace" When {
            "nested When namespace" should {
                "pass" {
                    1 shouldBe 1
                }

                "fail" {
                    1 shouldBe 2
                }
            }
        }

        "`when` namespace" `when` {
            "nested `when` namespace" should {
                "pass" {
                    1 shouldBe 1
                }

                "fail" {
                    1 shouldBe 2
                }
            }
        }

        "namespace" should {
            "pass" {
                1 shouldBe 1
            }

            "fail" {
                1 shouldBe 2
            }
        }
    })
