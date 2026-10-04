package io.github.codymikol.kotlintest.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

class JUnitAssumptionExample {
    @Test
    fun pass() {
        assertEquals(1, 1)
    }

    @Test
    fun assumption() {
        assumeTrue(false, "assumption is not met")
    }
}

class JUnitBeforeAllErrorExample {
    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            error("beforeAll failed")
        }
    }

    @Test
    fun pass() {
        assertEquals(1, 1)
    }
}

class JUnitNestedBeforeAllErrorExample {
    @Test
    fun pass() {
        assertEquals(1, 1)
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Namespace {
        @BeforeAll
        fun beforeAll() {
            error("nested beforeAll failed")
        }

        @Test
        fun pass() {
            assertEquals(1, 1)
        }
    }
}
