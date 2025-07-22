package io.github.codymikol.neotestkotlin.framework.kotest

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.RootTest
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

open class Subclass : Spec() {
    override fun globalExtensions(): List<Extension> = emptyList()

    override fun rootTests(): List<RootTest> = emptyList()
}

class SubclassSubclass : Subclass()

class KotestTestRunnerSpec :
    FunSpec({
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
