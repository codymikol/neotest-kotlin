package io.github.codymikol.neotestkotlin.framework

import kotlinx.serialization.Serializable
import java.time.Duration

@Serializable
@JvmInline
value class FullyQualifiedClassName(
    val value: String,
)

@Serializable
data class ClassResult(
    val name: FullyQualifiedClassName,
    val result: TestResultStatus,
    val tests: List<TestResult>,
) {
    val duration: Duration = tests.fold(Duration.ZERO) { acc, it -> acc + it.duration }
}
