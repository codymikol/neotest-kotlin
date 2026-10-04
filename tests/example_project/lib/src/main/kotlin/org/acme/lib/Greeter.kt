package org.acme.lib

class Greeter(
    private val name: String,
) {
    fun greet(): String = "Hello, $name!"
}
