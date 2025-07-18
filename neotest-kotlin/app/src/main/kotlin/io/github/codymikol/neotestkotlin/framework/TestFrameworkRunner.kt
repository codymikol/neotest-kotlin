package io.github.codymikol.neotestkotlin.framework

import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * Base interface for all test framework runners.
 */
interface TestFrameworkRunner {
    /**
     * Runs the provided test [classes] using the [TestFrameworkRunner].
     */
    fun run(classes: Collection<KClass<*>>): TestRunResult

    /**
     * Whether this class is runnable by this [TestFrameworkRunner].
     */
    fun isRunnable(kclass: KClass<*>): Boolean
}

typealias Report = Map<FullyQualifiedClassName, ClassResult>

@Serializable
sealed interface TestRunResult {
    @Serializable
    data class Success(
        val report: Report,
    ) : TestRunResult

    @Serializable
    object Failure : TestRunResult
}
