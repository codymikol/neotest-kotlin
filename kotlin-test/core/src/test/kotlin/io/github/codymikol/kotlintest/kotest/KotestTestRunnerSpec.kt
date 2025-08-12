package io.github.codymikol.kotlintest.kotest

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

class KotestTestRunnerSpec :
    FunSpec({
        context("toKotestFilter") {
            withData(
                mapOf(
                    "null" to Pair(null, null),
                    "top level" to Pair("org.example.TestExample::top level", "org.example.TestExample/top level"),
                    "single nested" to
                        Pair(
                            "org.example.TestExample::top level::test",
                            "org.example.TestExample/top level -- test",
                        ),
                    "double nested" to
                        Pair(
                            "org.example.TestExample::top level::namespace::test",
                            "org.example.TestExample/top level -- namespace -- test",
                        ),
                ),
            ) { (input, expected) ->
                input.toKotestFilter()?.let { DescriptorPaths.render(it).value } shouldBe expected
            }
        }

        context("isRunnable") {
            test("FunSpec subclass") {
                KotestTestRunner.isRunnable(KotestTestRunnerSpec::class) shouldBe true
            }

            test("Spec subclass") {
                KotestTestRunner.isRunnable(Subclass::class) shouldBe true
            }

            test("subclass of Spec subclass") {
                KotestTestRunner.isRunnable(SubclassSubclass::class) shouldBe true
            }

            test("fail") {
                KotestTestRunner.isRunnable(String::class) shouldBe false
            }
        }
    })
