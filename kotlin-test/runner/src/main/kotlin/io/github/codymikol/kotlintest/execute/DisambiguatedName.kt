package io.github.codymikol.kotlintest.execute

/**
 * The name discovery gives sibling tests/containers that share the same name, `#1`, `#2`, ...
 *
 * Must match `disambiguatedName` of discovery (`io.github.codymikol.kotlintest.discover`), which
 * the runner doesn't depend on.
 */
internal fun disambiguatedName(name: String, index: Int): String = "$name#$index"
