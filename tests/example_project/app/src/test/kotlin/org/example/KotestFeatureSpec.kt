package org.example

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class KotestFeatureSpec : FeatureSpec({
    feature("namespace") {
        scenario("pass") {
            "a" shouldBe "a"
        }

        scenario("fail") {
            "a" shouldBe "b"
        }

        feature("nested namespace") {
            scenario("pass") {
                "a" shouldBe "a"
            }

            scenario("fail") {
                "a" shouldBe "b"
            }
        }
    }
})
