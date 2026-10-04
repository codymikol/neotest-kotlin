package org.example

import io.kotest.matchers.shouldBe
import org.acme.testing.IntegrationFunSpec

class ModuleSubclassSpec : IntegrationFunSpec({
    context("from another project") {
        test("example") {
            1 shouldBe 1
        }
    }
})
