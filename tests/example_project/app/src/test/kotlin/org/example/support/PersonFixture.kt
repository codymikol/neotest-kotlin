package org.example.support

/**
 * Test fixture, this file contains no tests.
 */
data class Person(
    val name: String,
    val age: Int,
)

object PersonFixture {
    fun aPerson(name: String = "Jane"): Person = Person(name = name, age = 42)
}
