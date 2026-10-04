package io.github.codymikol.kotlintest.plugin

/**
 * Build file of a Kotlin JVM project with Kotest and JUnit Jupiter tests.
 */
fun kotlinJvmBuild(
    kotest: String = Versions.KOTEST,
    applyKotlin: Boolean = true,
): String =
    """
    plugins {
        kotlin("jvm")${if (applyKotlin) " version \"${Versions.KOTLIN}\"" else ""}
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("io.kotest:kotest-runner-junit5:$kotest")
        testImplementation("org.junit.jupiter:junit-jupiter:${Versions.JUNIT}")
    }

    tasks.test {
        useJUnitPlatform()
    }
    """

const val CALCULATOR = """
package com.example

class Calculator {
    fun add(a: Int, b: Int): Int = a + b
}
"""

const val CALCULATOR_SPEC = """
package com.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CalculatorSpec : FunSpec({
    context("add") {
        test("adds two numbers") {
            Calculator().add(1, 2) shouldBe 3
        }

        test("fails on purpose") {
            Calculator().add(1, 2) shouldBe 4
        }
    }
})
"""

const val CALCULATOR_TEST = """
package com.example

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CalculatorTest {
    @Test
    fun addsTwoNumbers() {
        assertEquals(3, Calculator().add(1, 2))
    }

    @Test
    fun failsOnPurpose() {
        assertEquals(4, Calculator().add(1, 2))
    }
}
"""

const val CALCULATOR_SPEC_PATH = "src/test/kotlin/com/example/CalculatorSpec.kt"
const val CALCULATOR_TEST_PATH = "src/test/kotlin/com/example/CalculatorTest.kt"

/**
 * Writes a Kotlin JVM project (at [path], the root project by default) with [CALCULATOR_SPEC] and [CALCULATOR_TEST].
 */
fun FunctionalTestProject.calculatorProject(
    path: String = "",
    buildFile: String = kotlinJvmBuild(),
) {
    val prefix = if (path.isEmpty()) "" else "$path/"
    file("${prefix}build.gradle.kts", buildFile)
    file("${prefix}src/main/kotlin/com/example/Calculator.kt", CALCULATOR)
    file("$prefix$CALCULATOR_SPEC_PATH", CALCULATOR_SPEC)
    file("$prefix$CALCULATOR_TEST_PATH", CALCULATOR_TEST)
}
