package io.github.codymikol.kotlintest.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Fixture with sibling tests and nested classes that share the same display name.
 *
 * JUnit doesn't run tests in source order, so duplicates are numbered by method/class name:
 * `same#1` is [a], `same#2` is [b], `group#1` is [First] and `group#2` is [Second].
 */
class JUnitDuplicateExample {
    @Test
    @DisplayName("same")
    fun b() {
        assertEquals(1, 2)
    }

    @Test
    @DisplayName("same")
    fun a() {
        assertEquals(1, 1)
    }

    @Test
    fun unique() {
        assertEquals(1, 1)
    }

    @Nested
    @DisplayName("group")
    inner class Second {
        @Test
        fun pass() {
            assertEquals(1, 1)
        }
    }

    @Nested
    @DisplayName("group")
    inner class First {
        @Test
        fun fail() {
            assertEquals(1, 2)
        }
    }
}
