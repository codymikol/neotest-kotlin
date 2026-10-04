package org.example

import io.kotest.core.spec.style.funSpec
import io.kotest.matchers.shouldBe

/**
 * Test factory, its tests are run (and discovered) as part of every spec that includes it.
 */
fun sharedTests() =
    funSpec {
        test("shared") {
            "a" shouldBe "a"
        }
    }
