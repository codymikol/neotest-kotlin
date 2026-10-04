package io.github.codymikol.kotlintest.execute.kotest

import io.github.codymikol.kotlintest.execute.disambiguatedName

/**
 * Kotest (with the default `DuplicateTestNameMode.Warn`) runs sibling tests that share a name as
 * `name`, `(1) name`, `(2) name`, ... while discovery reports them as `name#1`, `name#2`, `name#3`, ...
 *
 * These functions translate between the two.
 */
private val KOTEST_DUPLICATE_NAME = "^\\((\\d+)\\) (.*)$".toRegex(RegexOption.DOT_MATCHES_ALL)
private val DISCOVERED_DUPLICATE_NAME = "^(.*)#(\\d+)$".toRegex(RegexOption.DOT_MATCHES_ALL)

/**
 * Translates a Kotest test name to the discovered name given the Kotest names of all its
 * [siblings] (including itself).
 *
 * ```
 * pass       -> pass#1   (when '(1) pass' is a sibling)
 * (1) pass   -> pass#2   (when 'pass' is a sibling)
 * (2) pass   -> pass#3   (when 'pass' is a sibling)
 * other      -> other
 * ```
 */
internal fun String.toDiscoveredName(siblings: Set<String>): String {
    val kotestDuplicate = KOTEST_DUPLICATE_NAME.matchEntire(this)

    return when {
        kotestDuplicate != null && kotestDuplicate.groupValues[2] in siblings -> {
            val (index, name) = kotestDuplicate.destructured
            disambiguatedName(name, index.toInt() + 1)
        }
        "(1) $this" in siblings -> disambiguatedName(this, 1)
        else -> this
    }
}

/**
 * All Kotest names a discovered name could refer to, the name itself is always included in case
 * a test is literally named e.g. 'issue #1'.
 *
 * ```
 * pass#1 -> [pass#1, pass]
 * pass#2 -> [pass#2, (1) pass]
 * pass   -> [pass]
 * ```
 */
internal fun String.toKotestNames(): List<String> {
    val discoveredDuplicate = DISCOVERED_DUPLICATE_NAME.matchEntire(this) ?: return listOf(this)
    val (name, index) = discoveredDuplicate.destructured

    return when (val duplicateIndex = index.toIntOrNull()) {
        null, 0 -> listOf(this)
        1 -> listOf(this, name)
        else -> listOf(this, "(${duplicateIndex - 1}) $name")
    }
}
