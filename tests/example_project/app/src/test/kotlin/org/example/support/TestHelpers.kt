package org.example.support

/**
 * Helper used by tests, this file contains no tests.
 */
fun <T> retrying(
    times: Int = 3,
    block: () -> T,
): T {
    repeat(times - 1) {
        runCatching(block).onSuccess { return it }
    }

    return block()
}
