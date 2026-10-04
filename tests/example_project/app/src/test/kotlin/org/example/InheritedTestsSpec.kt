package org.example

import io.kotest.matchers.shouldBe

class InheritedTestsSpec :
    InheritedTestsBase({
        test("own") {
            "a" shouldBe "a"
        }

        include(sharedTests())
    })
