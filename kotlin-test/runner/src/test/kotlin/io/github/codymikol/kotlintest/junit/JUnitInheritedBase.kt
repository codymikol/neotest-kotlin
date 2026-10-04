package io.github.codymikol.kotlintest.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Abstract base class whose tests are run as part of every subclass, see [JUnitInheritedExample].
 */
abstract class JUnitInheritedBase {
    @Test
    fun inheritedPass() {
        assertEquals(1, 1)
    }

    @Test
    fun inheritedFail() {
        assertEquals(1, 2)
    }

    @Test
    open fun overridden() {
        assertEquals(1, 2)
    }

    @Nested
    inner class InheritedNested {
        @Test
        fun nestedInherited() {
            assertEquals(1, 1)
        }
    }
}
