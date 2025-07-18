package io.github.codymikol.neotestkotlin.framework

import kotlinx.serialization.Serializable
import java.time.Duration

@Serializable
data class TestResult(
    val name: String,
    val result: TestResultStatus,
    val duration: Duration,
)

@Serializable
sealed interface TestResultStatus {
    object Passed : TestResultStatus

    @Serializable
    data class Failure(
        val cause: Throwable,
    ) : TestResultStatus

    @Serializable
    data class Skipped(
        val reason: String?,
    ) : TestResultStatus
}
