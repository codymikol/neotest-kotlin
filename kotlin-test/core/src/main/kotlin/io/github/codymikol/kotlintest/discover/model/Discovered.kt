package io.github.codymikol.kotlintest.discover.model

public sealed interface Discovered {
    /**
     * Unique identifier for the test `::` separation signifies nesting.
     */
    public val id: String

    /**
     * The last segment of the [id] where applicable for specific tests or a more readable
     * version of the name.
     */
    public val name: String

    /**
     * File location and position in that file for the [Discovered].
     */
    public val position: Position

    /**
     * The type of test used to serialization/deserialization purposes.
     */
    public val type: TestType

    public data class Test(
        override val id: String,
        override val name: String,
        override val position: Position,
    ) : Discovered {
        override val type: TestType = TestType.TEST
    }

    /**
     * A namespace/container that could be a class or
     * a nested test.
     */
    public data class Container(
        override val id: String,
        override val name: String,
        override val position: Position,
        /**
         * Nested [Discovered] under this [Container].
         */
        val tests: Set<Discovered>,
    ) : Discovered {
        override val type: TestType = TestType.CONTAINER
    }
}
