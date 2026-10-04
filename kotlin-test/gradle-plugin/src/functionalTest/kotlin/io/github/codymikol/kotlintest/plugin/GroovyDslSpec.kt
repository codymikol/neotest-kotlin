package io.github.codymikol.kotlintest.plugin

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.maps.shouldContainExactly

class GroovyDslSpec : FunSpec({
    test("discovers and runs tests of a Groovy DSL build") {
        val project = FunctionalTestProject(tempdir())
        project.file(
            "settings.gradle",
            """
            rootProject.name = 'groovy-dsl'
            """,
        )
        project.file(
            "build.gradle",
            """
            plugins {
                id 'org.jetbrains.kotlin.jvm' version '${Versions.KOTLIN}'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testImplementation 'io.kotest:kotest-runner-junit5:${Versions.KOTEST}'
            }

            tasks.named('test') {
                useJUnitPlatform()
            }
            """,
        )
        project.file("src/main/kotlin/com/example/Calculator.kt", CALCULATOR)
        project.file(CALCULATOR_SPEC_PATH, CALCULATOR_SPEC)
        val output = project.dir.resolve("build/execution.json")

        project.build(project.discoverArguments(CALCULATOR_SPEC_PATH))
        project.build(
            "kotlinTestExecute",
            "-Pclasses=com.example.CalculatorSpec",
            "-PoutputFile=${output.absolutePath}",
            "--configuration-cache",
        )

        val discovered = readJson(project.discoveryOutput(CALCULATOR_SPEC_PATH)).discoveredTests()
        discovered.filterValues { it == "TEST" } shouldContainExactly
            mapOf(
                "com.example.CalculatorSpec::add::adds two numbers" to "TEST",
                "com.example.CalculatorSpec::add::fails on purpose" to "TEST",
            )
        readJson(output).executionResults() shouldContainExactly
            mapOf(
                "com.example.CalculatorSpec::add::adds two numbers" to "SUCCESS",
                "com.example.CalculatorSpec::add::fails on purpose" to "FAILURE",
            )
    }
})
