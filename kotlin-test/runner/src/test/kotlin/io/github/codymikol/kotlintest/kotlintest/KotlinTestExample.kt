package io.github.codymikol.kotlintest.kotlintest

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class KotlinTestExample {
    @Test
    fun pass() {
        assertEquals(1, 1)
    }

    @Test
    fun fail() {
        assertEquals(1, 2)
    }
}
