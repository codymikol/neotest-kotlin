package io.github.codymikol.kotlintestlauncher

import io.kotest.core.test.TestResult
import kotlin.time.Duration

/**
 * A test node, the parent union type of [TestNode.Test] and a [TestNode.Container]
 * where a [TestNode.Test] is a terminal and a [TestNode.Container] can contain multiple
 * other containers or tests.
 */
public sealed interface TestNode {
    public val name: String

    /**
     * The total execution time of the [TestNode.Test] or all [TestNode.Container.nodes].
     */
    public val duration: Duration
    public val type: TestNodeType

    public enum class TestNodeType {
        CONTAINER,
        TEST,
    }

    /**
     * Terminal node.
     */
    public data class Test(
        override val name: String,
        override val duration: Duration,
        public val status: TestStatus,
    ) : TestNode {
        override val type: TestNodeType = TestNodeType.TEST
    }

    /**
     * Can contain multiple other [Container]s or [Test]s inside of [nodes].
     * This can be the root class node, top level namespace, or a nested namespace.
     */
    public data class Container(
        override val name: String,
    ) : TestNode {
        private val nodes: MutableList<TestNode> = mutableListOf()
        public val tests: List<TestNode>
            get() = nodes.toList()

        override val type: TestNodeType = TestNodeType.CONTAINER

        override val duration: Duration
            get() = this.nodes.fold(Duration.ZERO) { acc, it -> acc + it.duration }

        public val status: Status
            get() {
                val tests: List<TestNode.Test> = this.visitAllNodes().filterIsInstance(TestNode.Test::class.java).toList()
                return when {
                    tests.all { it.status == TestStatus.Success } -> Status.SUCCESS
                    tests.any { it.status is TestStatus.Failure } -> Status.FAILURE
                    else -> Status.IGNORED
                }
            }

        internal fun visitAllNodes(): Sequence<TestNode> {
            val parent = this

            return sequence {
                val queue = mutableListOf<TestNode.Container>(parent)
                while (queue.isNotEmpty()) {
                    val container = queue.removeFirst()

                    container.nodes.forEach { child ->
                        yield(child)

                        if (child is TestNode.Container) {
                            queue.add(child)
                        }
                    }
                }
            }
        }

        /**
         * Adds the [node] to the [Container] appending onto potentially nested [TestNode.Container]s
         * specified by [parentNames]. An empty list for [parentNames] means that it's top level.
         *
         * @throws IllegalArgumentException [parentNames] references a non-existent node or a node that
         * isn't a [TestNode.Container].
         */
        @Throws(IllegalArgumentException::class)
        internal fun add(
            node: TestNode,
            parentNames: List<String> = emptyList(),
        ): Boolean {
            val child =
                parentNames.fold(this) { currentNode, parentName ->
                    val child =
                        requireNotNull(currentNode.nodes.find { it.name == parentName }) {
                            "${this.name} > ${parentNames.joinToString(separator = " > ")} does not exist"
                        }

                    require(child is TestNode.Container) {
                        "${this.name} > ${parentNames.joinToString(separator = " > ")} is not a TestNode.Container"
                    }

                    child
                }

            require(
                !child.nodes.any { it.name == node.name },
            ) { "${this.name} > ${parentNames.joinToString(separator = " > ")} > ${node.name} already exists" }

            return child.nodes.add(node)
        }
    }
}

public enum class Status {
    SUCCESS,
    FAILURE,
    IGNORED,
}

public sealed interface TestStatus {
    public val status: Status

    public object Success : TestStatus {
        override val status: Status = Status.SUCCESS
    }

    public companion object {
        internal fun from(kotestResult: TestResult): TestStatus =
            when (kotestResult) {
                is TestResult.Success -> Success
                is TestResult.Failure -> {
                    val error = kotestResult.errorOrNull

                    Failure(
                        stackTrace = error?.stackTraceToString(),
                        error =
                            error?.let {
                                val traceOrigin = it.stackTrace?.firstOrNull()

                                Failure.Error(
                                    message = it.message,
                                    lineNumber = traceOrigin?.lineNumber,
                                    filename = traceOrigin?.fileName,
                                )
                            },
                    )
                }
                is TestResult.Ignored -> Ignored(reason = kotestResult.reason)
                is TestResult.Error -> TODO()
            }
    }

    public data class Failure(
        /**
         * The entire stacktrace as a String, this is useful for displaying what
         * a thrown exception would yield in the console.
         */
        public val stackTrace: String?,
        /**
         * Programmatic view of the original error.
         */
        public val error: Error?,
    ) : TestStatus {
        override val status: Status = Status.FAILURE

        public data class Error(
            public val message: String?,
            public val lineNumber: Int?,
            public val filename: String?,
        )
    }

    public data class Ignored(
        public val reason: String? = null,
    ) : TestStatus {
        override val status: Status = Status.IGNORED
    }
}
