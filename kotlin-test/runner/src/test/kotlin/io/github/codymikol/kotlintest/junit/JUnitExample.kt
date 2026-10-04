package io.github.codymikol.kotlintest.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertAll

class JUnitExample {
    @Test
    fun pass() {
        assertEquals(1, 1)
    }

    @Test
    fun fail() {
        assertEquals(1, 2)
    }

    @Nested
    inner class NestedJUnitExample {
        @Test
        fun pass() {
            assertEquals(1, 1)
        }

        @Test
        fun fail() {
            assertEquals(1, 2)
        }

        @Nested
        inner class NestedNestedJUnitExample {
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

    @Test
    @DisplayName("test name with spaces")
    fun nameIsReplacedWithDisplayName() {
        assertEquals(1, 1)
    }

    @Test
    @Disabled
    fun disabled() {
        assertEquals(1, 3)
    }

    @Test
    fun softAssertions() {
        assertAll(
            { assertEquals(1, 4) },
            { assertEquals(1, 4) },
            { assertEquals(1, 4) },
            { assertEquals(1, 4) },
            { assertEquals(1, 4) },
        )
    }

    @Test
    fun `kotlin escaped string pass`() {
        assertEquals(1, 1)
    }

    @TestFactory
    fun testSquares() =
        listOf(
            1 to 1,
            2 to 4,
            3 to 9,
            4 to 16,
            5 to 26,
        ).map { (input, expected) ->
            DynamicTest.dynamicTest("$input^2 = $expected") {
                assertEquals(expected, input * input)
            }
        }
}
