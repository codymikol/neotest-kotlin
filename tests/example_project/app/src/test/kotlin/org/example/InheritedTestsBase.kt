package org.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Abstract base spec whose tests are run (and discovered) as part of every subclass.
 */
abstract class InheritedTestsBase(
    body: InheritedTestsBase.() -> Unit,
) : FunSpec() {
    init {
        test("inherited") {
            "a" shouldBe "a"
        }

        body()

        test("inherited fail") {
            "a" shouldBe "b"
        }
    }
}
