package io.github.codymikol.neotestkotlin

import io.github.codymikol.neotestkotlin.framework.ClassResult
import io.github.codymikol.neotestkotlin.framework.FullyQualifiedClassName
import io.github.codymikol.neotestkotlin.framework.TestFrameworkRunner
import io.github.codymikol.neotestkotlin.framework.TestRunResult
import io.github.codymikol.neotestkotlin.framework.kotest.KotestTestRunner
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

fun main(args: List<String>) {
    val classes: List<KClass<*>> =
        listOf(
            Class.forName("org.example.KotestFunSpec").kotlin,
        )

    val supportedFrameworks: List<TestFrameworkRunner> =
        listOf(
            KotestTestRunner,
        )

    val results: Map<FullyQualifiedClassName, ClassResult> =
        supportedFrameworks
            .map { runner ->
                val runnableClasses = classes.filter { runner.isRunnable(it) }

                val result = runner.run(runnableClasses)
                when (result) {
                    is TestRunResult.Success -> result.report
                    is TestRunResult.Failure -> TODO()
                }
            }.fold(emptyMap<FullyQualifiedClassName, ClassResult>()) { acc, it -> acc + it }

    println(Json.encodeToString(results))
}
