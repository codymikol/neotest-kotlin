package io.github.codymikol.kotlintestlauncher

import io.kotest.core.test.TestResult
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlin.time.Duration

/**
 * A test node, the parent union type of a [TestNode.Test] and a [TestNode.Container]
 * where a [TestNode.Test] is a terminal and a [TestNode.Container] can contain multiple
 * other namespaces or tests.
 */
@OptIn(ExperimentalSerializationApi::class) // JsonClassDiscriminator is an experimental annotation for now
@JsonClassDiscriminator("type")
@Serializable
public sealed interface TestNode {
    public val name: String

    /**
     * The total execution time of the [TestNode.Test] or all [TestNode.Container.nodes].
     */
    public val duration: Duration

    /**
     * Terminal node.
     */
    @Serializable
    @SerialName("test")
    public data class Test(
        override val name: String,
        override val duration: Duration,
        public val status: TestStatus,
    ) : TestNode

    /**
     * Can contain multiple other [Container]s or [Test]s inside of [nodes].
     * This can be the root class node, top level namespace, or a nested namespace.
     */
    @Serializable
    @SerialName("container")
    public data class Container(
        override val name: String,
    ) : TestNode {
        private val nodes: MutableList<TestNode> = mutableListOf()

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

@Serializable
public enum class Status {
    SUCCESS,
    FAILURE,
    IGNORED,
}

@Serializable
@OptIn(ExperimentalSerializationApi::class) // JsonClassDiscriminator is an experimental annotation for now
@JsonClassDiscriminator("status")
public sealed interface TestStatus {
    @Serializable
    @SerialName("success")
    public object Success : TestStatus

    public companion object {
        internal fun from(kotestResult: TestResult): TestStatus =
            when (kotestResult) {
                is TestResult.Success -> TestStatus.Success
                is TestResult.Failure -> {
                    val error = kotestResult.errorOrNull

                    TestStatus.Failure(
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
                is TestResult.Ignored -> TestStatus.Ignored(reason = kotestResult.reason)
                is TestResult.Error -> TODO()
            }
    }

    @Serializable
    @SerialName("failure")
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
        @Serializable
        public data class Error(
            public val message: String?,
            public val lineNumber: Int?,
            public val filename: String?,
        )
    }

    @Serializable
    @SerialName("ignored")
    public data class Ignored(
        public val reason: String? = null,
    ) : TestStatus
}
