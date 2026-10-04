package io.github.codymikol.kotlintest.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Fixture with tests inherited from [JUnitInheritedBase].
 */
class JUnitInheritedExample : JUnitInheritedBase() {
    @Test
    fun own() {
        assertEquals(1, 1)
    }

    // not annotated with @Test, so JUnit doesn't run it (nor the overridden test)
    override fun overridden() {
        assertEquals(1, 1)
    }
}
