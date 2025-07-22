package io.github.codymikol.neotestkotlin

import io.github.codymikol.neotestkotlin.framework.TestFrameworkRunner
import io.github.codymikol.neotestkotlin.framework.TestNode
import io.kotest.common.runBlocking
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

fun main(args: List<String>) {
    val classes: List<KClass<*>> =
        listOf(
            Class.forName("org.example.KotestFunSpec").kotlin,
        )

    val results: Set<TestNode.Container> =
        runBlocking {
            TestFrameworkRunner.runAll(classes)
        }

    println(Json.encodeToString(results))
}
