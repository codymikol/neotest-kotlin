package org.example

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class KotestDescribeSpec : DescribeSpec({
    describe("a namespace") {
        it("should handle failed assertions") {
            "a" shouldBe "b"
        }

        it("should handle passed assertions") {
            "a" shouldBe "a"
        }

        xit("should handle skipped assertions") {
            "a" shouldBe "a"
        }

        describe("a nested namespace") {
            it("should handle failed assertions") {
                "a" shouldBe "b"
            }

            it("should handle passed assertions") {
                "a" shouldBe "a"
            }

            xit("should handle skipped assertions") {
                "a" shouldBe "a"
            }
        }
    }
})
