package io.github.codymikol.kotlintest.kotest

import io.kotest.matchers.shouldBe

/**
 * Fixture with tests inherited from [KotestInheritedBase] and included from test factories, see
 * [sharedFactory] and [sharedFactoryProperty].
 */
class KotestInheritedExample :
    KotestInheritedBase({
        test("own") {
            "a" shouldBe "a"
        }

        include(sharedFactory())
        include("prefixed", sharedFactoryProperty)
    }) {
    init {
        test("own init") {
            "a" shouldBe "a"
        }
    }
}
