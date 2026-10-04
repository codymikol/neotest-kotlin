package io.github.codymikol.kotlintest.discover.junit

/**
 * Orders sibling test methods and `@Nested` classes that share a display name.
 *
 * JUnit doesn't execute tests in source order and its `TestIdentifier`s don't carry line numbers,
 * so both discovery and the runner's `JUnitTestReporter` number duplicate display names (`#1`, `#2`, ...)
 * by this key that is known at both discovery time (from source) and execution time (from the
 * `TestIdentifier`'s source): methods before nested classes, then by method/class name, then by the
 * number of method parameters (overloads).
 *
 * The runner, which doesn't depend on this module, has its own copy of this ordering
 * (`io.github.codymikol.kotlintest.execute.junit.JUnitSiblingKey`), both must be kept in sync.
 */
internal data class JUnitSiblingKey(
    private val kind: Kind,
    private val name: String,
    private val parameterCount: Int,
) : Comparable<JUnitSiblingKey> {
    internal enum class Kind {
        METHOD,
        NESTED_CLASS,
    }

    override fun compareTo(other: JUnitSiblingKey): Int =
        compareValuesBy(this, other, { it.kind }, { it.name }, { it.parameterCount })

    internal companion object {
        fun method(name: String, parameterCount: Int): JUnitSiblingKey =
            JUnitSiblingKey(Kind.METHOD, name, parameterCount)

        fun nestedClass(simpleName: String): JUnitSiblingKey = JUnitSiblingKey(Kind.NESTED_CLASS, simpleName, 0)
    }
}
