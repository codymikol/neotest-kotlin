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
    ) : Discovered, Iterable<Discovered> {
        override val type: TestType = TestType.CONTAINER

        public fun duplicateTestWarnings(): List<TestWarning> =
            this
                .groupBy { it.id }
                .filterValues { it.size >= 2 }
                .flatMap { (_, tests) ->
                    tests.drop(1).map { duplicateTest ->
                        TestWarning(
                            message = "Multiple tests defined with name '${duplicateTest.name}'",
                            position = duplicateTest.position
                        )
                    }
                }

        override fun iterator(): Iterator<Discovered> = iterator {
            tests.forEach { discovered ->
                when (discovered) {
                    is Container -> {
                        yield(discovered)
                        yieldAll(discovered.iterator())
                    }
                    is Test -> yield(discovered)
                }
            }
        }
    }
}
