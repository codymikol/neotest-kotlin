// intentionally doesn't match its directory to test running directories with mixed packages
package org.other

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class OtherPackageSpec : FunSpec({
    test("pass") {
        1 shouldBe 1
    }
})
