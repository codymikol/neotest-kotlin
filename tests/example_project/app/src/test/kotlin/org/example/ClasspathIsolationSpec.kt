package org.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.io.File

/**
 * Verifies that tests run with this project's own test frameworks and Kotlin standard library,
 * rather than versions bundled with neotest-kotlin.
 */
class ClasspathIsolationSpec :
    FunSpec({
        test("Kotlin compiler is not on the classpath") {
            runCatching { Class.forName("org.jetbrains.kotlin.psi.KtFile") }.isFailure shouldBe true
        }

        test("Kotest engine comes from the project") {
            jarOf("io.kotest.engine.TestEngineLauncher") shouldStartWith "kotest-framework-engine"
        }

        test("JUnit Platform launcher matches the JUnit Platform engine") {
            val platformVersion = jarOf("org.junit.platform.engine.TestEngine").removePrefix("junit-platform-engine-")

            jarOf("org.junit.platform.launcher.core.LauncherFactory") shouldBe "junit-platform-launcher-$platformVersion"
        }

        test("kotlin-stdlib comes from the project") {
            jarOf("kotlin.KotlinVersion") shouldStartWith "kotlin-stdlib-"
        }
    })

private fun jarOf(className: String): String =
    File(
        Class
            .forName(className)
            .protectionDomain.codeSource.location
            .toURI(),
    ).name
