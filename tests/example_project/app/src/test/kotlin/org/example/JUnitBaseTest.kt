package org.example

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Abstract base class whose tests are run (and discovered) as part of every subclass.
 */
abstract class JUnitBaseTest {
    @Test
    fun inheritedPass() {
        assertEquals(1, 1)
    }
}
