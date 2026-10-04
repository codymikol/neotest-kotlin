package io.github.codymikol.kotlintest.kotest

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
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
