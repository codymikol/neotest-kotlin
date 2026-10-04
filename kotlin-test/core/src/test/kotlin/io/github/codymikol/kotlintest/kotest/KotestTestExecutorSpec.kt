package io.github.codymikol.kotlintest.kotest

import io.github.codymikol.kotlintest.execute.kotest.KotestTestExecutor
import io.github.codymikol.kotlintest.execute.kotest.toDiscoveredName
import io.github.codymikol.kotlintest.execute.kotest.toKotestFilters
import io.github.codymikol.kotlintest.execute.kotest.toKotestNames
import io.kotest.core.descriptors.DescriptorPaths
import io.kotest.core.spec.RootTest
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

open class Subclass : Spec() {
    override fun rootTests(): List<RootTest> = emptyList()
}

class SubclassSubclass : Subclass()

class KotestTestExecutorSpec :
    FunSpec({
        context("toKotestFilters") {
            withData(
                mapOf(
                    "null" to Pair(null, emptyList()),
                    "class" to Pair("org.example.TestExample", listOf("org.example.TestExample")),
                    "top level" to
                        Pair("org.example.TestExample::top level", listOf("org.example.TestExample/top level")),
                    "single nested" to
                        Pair(
                            "org.example.TestExample::top level::test",
                            listOf("org.example.TestExample/top level -- test"),
                        ),
                    "double nested" to
                        Pair(
                            "org.example.TestExample::top level::namespace::test",
                            listOf("org.example.TestExample/top level -- namespace -- test"),
                        ),
                    "first duplicate" to
                        Pair(
                            "org.example.TestExample::test#1",
                            listOf("org.example.TestExample/test#1", "org.example.TestExample/test"),
                        ),
                    "nested duplicates" to
                        Pair(
                            "org.example.TestExample::context#2::test#3",
                            listOf(
                                "org.example.TestExample/context#2 -- test#3",
                                "org.example.TestExample/context#2 -- (2) test",
                                "org.example.TestExample/(1) context -- test#3",
                                "org.example.TestExample/(1) context -- (2) test",
                            ),
                        ),
                ),
            ) { (input, expected) ->
                input.toKotestFilters().map { DescriptorPaths.render(it).value } shouldBe expected
            }
        }

        context("toKotestNames") {
            withData(
                mapOf(
                    "unique" to Pair("test", listOf("test")),
                    "first duplicate" to Pair("test#1", listOf("test#1", "test")),
                    "second duplicate" to Pair("test#2", listOf("test#2", "(1) test")),
                    "third duplicate" to Pair("test#3", listOf("test#3", "(2) test")),
                    "zero" to Pair("test#0", listOf("test#0")),
                    "hash in name" to Pair("issue #12 regression", listOf("issue #12 regression")),
                ),
            ) { (input, expected) ->
                input.toKotestNames() shouldBe expected
            }
        }

        context("toDiscoveredName") {
            withData(
                mapOf(
                    "unique" to Triple("test", setOf("test", "other"), "test"),
                    "first duplicate" to Triple("test", setOf("test", "(1) test"), "test#1"),
                    "second duplicate" to Triple("(1) test", setOf("test", "(1) test"), "test#2"),
                    "third duplicate" to Triple("(2) test", setOf("test", "(1) test", "(2) test"), "test#3"),
                    "literal parenthesis" to Triple("(1) test", setOf("(1) test"), "(1) test"),
                ),
            ) { (name, siblings, expected) ->
                name.toDiscoveredName(siblings) shouldBe expected
            }
        }

        context("isRunnable") {
            test("FunSpec subclass") {
                KotestTestExecutor.isRunnable(KotestTestExecutorSpec::class) shouldBe true
            }

            test("Spec subclass") {
                KotestTestExecutor.isRunnable(Subclass::class) shouldBe true
            }

            test("subclass of Spec subclass") {
                KotestTestExecutor.isRunnable(SubclassSubclass::class) shouldBe true
            }

            test("fail") {
                KotestTestExecutor.isRunnable(String::class) shouldBe false
            }
        }
    })
