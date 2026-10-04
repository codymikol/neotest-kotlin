package org.example

import org.acme.lib.Greeter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LibJUnitTest {
    @Test
    fun pass() {
        assertEquals("Hello, lib!", Greeter("lib").greet())
    }

    @Test
    fun fail() {
        assertEquals("Hello, app!", Greeter("lib").greet())
    }
}
