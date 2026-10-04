package org.example

import io.kotest.matchers.shouldBe
import org.acme.lib.Greeter
import org.acme.testing.IntegrationFunSpec

class LibFunSpec : IntegrationFunSpec({
    context("greeter") {
        test("pass") {
            Greeter("lib").greet() shouldBe "Hello, lib!"
        }

        test("fail") {
            Greeter("lib").greet() shouldBe "Hello, app!"
        }
    }
})
