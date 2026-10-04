package io.github.codymikol.kotlintest.kotest

import io.kotest.core.spec.style.FunSpec

class KotestBeforeSpecErrorExample :
    FunSpec({
        beforeSpec {
            error("beforeSpec failed")
        }

        test("pass") {}

        context("namespace") {
            test("pass") {}
        }
    })

class KotestConstructorErrorExample :
    FunSpec({
        test("pass") {}
    }) {
    init {
        error("constructor failed")
    }
}

class KotestBeforeTestErrorExample :
    FunSpec({
        beforeTest {
            error("beforeTest failed")
        }

        test("pass") {}
    })
