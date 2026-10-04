package io.github.codymikol.kotlintest.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Disabled
class JUnitDisabledExample {
    @Test
    fun pass() {
        assertEquals(1, 1)
    }

    @Test
    fun fail() {
        assertEquals(1, 2)
    }

    @Nested
    inner class NestedJUnitDisabledExample {
        @Test
        fun pass() {
            assertEquals(1, 1)
        }

        @Test
        fun fail() {
            assertEquals(1, 2)
        }

        @Nested
        inner class NestedNestedJUnitDisabledExample {
            @Test
            fun pass() {
                assertEquals(1, 1)
            }

            @Test
            fun fail() {
                assertEquals(1, 2)
            }
        }
    }
}
