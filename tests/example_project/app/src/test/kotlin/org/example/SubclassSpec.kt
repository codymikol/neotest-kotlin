package org.example

import io.kotest.matchers.shouldBe

class SubclassSpec : ParentKotestFunSpec({
    test("example") {
        1 shouldBe 1
    }
})
