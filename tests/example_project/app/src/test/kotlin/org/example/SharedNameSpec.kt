package org.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Has the same fully qualified name as a spec of `lib`, but different tests.
 */
class SharedNameSpec : FunSpec({
    test("from app") {
        "app" shouldBe "app"
    }
})
