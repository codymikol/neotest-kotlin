package io.github.codymikol.kotlintest.plugin

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class ExecutionCompatibilitySpec : FunSpec({
    context("junitPlatformLauncherFor") {
        test("launcher already present") {
            ExecutionCompatibility.junitPlatformLauncherFor(
                mapOf(
                    "org.junit.platform:junit-platform-engine" to "6.0.2",
                    "org.junit.platform:junit-platform-launcher" to "6.0.2",
                ),
            ) shouldBe null
        }

        test("launcher missing uses the JUnit Platform engine version") {
            ExecutionCompatibility.junitPlatformLauncherFor(
                mapOf(
                    "org.junit.platform:junit-platform-commons" to "1.11.0",
                    "org.junit.platform:junit-platform-engine" to "1.11.4",
                ),
            ) shouldBe "org.junit.platform:junit-platform-launcher:1.11.4"
        }

        test("launcher missing falls back to the JUnit Platform commons version") {
            ExecutionCompatibility.junitPlatformLauncherFor(
                mapOf("org.junit.platform:junit-platform-commons" to "6.0.0"),
            ) shouldBe "org.junit.platform:junit-platform-launcher:6.0.0"
        }

        test("no JUnit Platform") {
            ExecutionCompatibility.junitPlatformLauncherFor(emptyMap()) shouldBe null
        }
    }

    context("unsupportedKotestMessage") {
        context("supported") {
            withData("6.1.0", "6.1.0-RC1", "6.1.2", "6.2.5", "7.0.0") { version ->
                ExecutionCompatibility.unsupportedKotestMessage(
                    projectPath = ":app",
                    modules = mapOf("io.kotest:kotest-framework-engine-jvm" to version),
                ) shouldBe null
            }
        }

        context("unsupported") {
            withData("5.9.1", "6.0.0", "6.0.7", "6.0.0.M17") { version ->
                val message =
                    ExecutionCompatibility.unsupportedKotestMessage(
                        projectPath = ":app",
                        modules = mapOf("io.kotest:kotest-framework-engine-jvm" to version),
                    )

                message shouldNotBe null
                message shouldContain "Kotest 6.1.0 or newer"
                message shouldContain "':app'"
                message shouldContain "io.kotest:kotest-framework-engine-jvm:$version"
            }
        }

        test("no Kotest") {
            ExecutionCompatibility.unsupportedKotestMessage(":app", emptyMap()) shouldBe null
        }

        test("unknown version scheme") {
            ExecutionCompatibility.unsupportedKotestMessage(
                ":app",
                mapOf("io.kotest:kotest-framework-engine" to "latest"),
            ) shouldBe null
        }
    }
})
