package io.github.codymikol.neotestkotlin.framework.kotest

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.ints.shouldBeEven
import io.kotest.matchers.ints.shouldBeOdd
import io.kotest.matchers.shouldBe

class KotestExample :
    FunSpec({
        test("pass") {
            1.shouldBeOdd()
        }

        test("fail") {
            1.shouldBeEven()
        }

        context("top level") {
            test("pass") {
                1.shouldBeOdd()
            }

            test("fail") {
                1.shouldBeEven()
            }

            withData(
                mapOf(
                    "1 == 1" to row(1, 1),
                    "1 == 2" to row(1, 2),
                    "1 == 3" to row(1, 3),
                    "1 == 4" to row(1, 4),
                ),
            ) { (input, expected) ->
                input shouldBe expected
            }

            test("assert softly") {
                assertSoftly {
                    1.shouldBeEven()
                    1 shouldBe 2
                    1 shouldBe 3
                }
            }

            context("nested") {
                test("pass") {
                    1.shouldBeOdd()
                }

                test("fail") {
                    1.shouldBeEven()
                }
            }

            xtest("ignored test") {
                1.shouldBeOdd()
            }

            xcontext("ignored context") {
                xtest("ignored") {
                    1.shouldBeOdd()
                }
            }
        }
    })
