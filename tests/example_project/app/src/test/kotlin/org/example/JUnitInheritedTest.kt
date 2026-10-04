package org.example

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JUnitInheritedTest : JUnitBaseTest() {
    @Test
    fun own() {
        assertEquals(1, 1)
    }
}
