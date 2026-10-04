package io.github.codymikol.kotlintest.plugin

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.TaskOutcome

/**
 * A build whose root project declares the Kotlin plugin without applying it, like the example project.
 * The init script applies the plugin to the root project and to every subproject.
 */
class MultiProjectSpec : FunSpec({
    val project = FunctionalTestProject(tempdir())
    project.settings("app", "lib")
    project.file(
        "build.gradle.kts",
        """
        plugins {
            kotlin("jvm") version "${Versions.KOTLIN}" apply false
        }
        """,
    )
    project.file(
        "lib/build.gradle.kts",
        """
        plugins {
            kotlin("jvm")
        }

        repositories {
            mavenCentral()
        }
        """,
    )
    project.file("lib/src/main/kotlin/com/example/Calculator.kt", CALCULATOR)
    project.file(
        "app/build.gradle.kts",
        kotlinJvmBuild(applyKotlin = false).replace(
            "dependencies {",
            "dependencies {\n        testImplementation(project(\":lib\"))",
        ),
    )
    project.file("app/$CALCULATOR_SPEC_PATH", CALCULATOR_SPEC)

    test("kotlinTestExecute is registered once per Kotlin project") {
        val result = project.build("tasks", "--all")

        Regex("""^(\S+:)?kotlinTestExecute\b""", RegexOption.MULTILINE)
            .findAll(result.output)
            .map { it.value }
            .toList() shouldBe listOf("app:kotlinTestExecute", "lib:kotlinTestExecute")
    }

    test("discovery resolves classes of other projects") {
        val path = "app/$CALCULATOR_SPEC_PATH"
        val result = project.build(discoverTask(path), "--configuration-cache")

        result.output shouldContain "Configuration cache entry stored."
        val discovered = readJson(project.discoveryOutput(path)).discoveredTests()
        discovered.filterValues { it == "TEST" } shouldContainExactly
            mapOf(
                "com.example.CalculatorSpec::add::adds two numbers" to "TEST",
                "com.example.CalculatorSpec::add::fails on purpose" to "TEST",
            )
    }

    test("kotlinTestExecute runs the tests of a subproject") {
        val output = project.dir.resolve("build/execution.json")

        val result =
            project.build(
                ":app:kotlinTestExecute",
                "-Pclasses=com.example.CalculatorSpec",
                "-PoutputFile=${output.absolutePath}",
                "--configuration-cache",
            )

        result.task(":app:kotlinTestExecute")?.outcome shouldBe TaskOutcome.SUCCESS
        readJson(output).executionResults() shouldContainExactly
            mapOf(
                "com.example.CalculatorSpec::add::adds two numbers" to "SUCCESS",
                "com.example.CalculatorSpec::add::fails on purpose" to "FAILURE",
            )
    }
})
