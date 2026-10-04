package org.example.mixed

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MixedPackageSpec : FunSpec({
    test("pass") {
        1 shouldBe 1
    }
})
