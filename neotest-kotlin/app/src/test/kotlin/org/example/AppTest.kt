package org.example

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class AppTest {
    @Test
    fun appHasAGreeting() {
        val classUnderTest = App()
        assertNotNull(classUnderTest.greeting, "app should have a greeting")
    }
}
