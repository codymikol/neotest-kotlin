package io.github.codymikol.kotlintest.kotest

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Fixture with sibling tests and containers that share the same name.
 *
 * Discovery disambiguates these as `pass#1`, `pass#2`, `pass#3`, `context#1`, `context#2`, while
 * Kotest itself runs them as `pass`, `(1) pass`, `(2) pass`, `context`, `(1) context`.
 */
class KotestDuplicateExample :
    FunSpec({
        test("pass") {
            "a" shouldBe "a"
        }

        test("pass") {
            "a" shouldBe "b"
        }

        test("pass") {
            "a" shouldBe "a"
        }

        test("unique") {
            "a" shouldBe "a"
        }

        context("context") {
            test("dup") {
                "a" shouldBe "a"
            }

            test("dup") {
                "a" shouldBe "b"
            }
        }

        context("context") {
            test("dup") {
                "a" shouldBe "b"
            }
        }
    })
