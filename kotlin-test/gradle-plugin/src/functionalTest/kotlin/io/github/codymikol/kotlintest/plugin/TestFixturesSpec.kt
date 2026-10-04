package io.github.codymikol.kotlintest.plugin

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.string.shouldContain

/**
 * Discovery of a spec extending a base spec of the test fixtures of another project, which itself uses the
 * production code of a third project.
 */
class TestFixturesSpec : FunSpec({
    test("discovery resolves the test fixtures of other projects") {
        val project = FunctionalTestProject(tempdir())
        project.settings("app", "fixtures", "core")
        project.file(
            "build.gradle.kts",
            """
            plugins {
                kotlin("jvm") version "${Versions.KOTLIN}" apply false
            }
            """,
        )
        project.file(
            "core/build.gradle.kts",
            """
            plugins {
                kotlin("jvm")
            }

            repositories {
                mavenCentral()
            }
            """,
        )
        project.file("core/src/main/kotlin/com/example/Calculator.kt", CALCULATOR)
        project.file(
            "fixtures/build.gradle.kts",
            """
            plugins {
                kotlin("jvm")
                `java-test-fixtures`
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                testFixturesApi("io.kotest:kotest-runner-junit5:${Versions.KOTEST}")
                testFixturesApi(project(":core"))
            }
            """,
        )
        project.file(
            "fixtures/src/testFixtures/kotlin/com/example/fixtures/CalculatorBaseSpec.kt",
            """
            package com.example.fixtures

            import com.example.Calculator
            import io.kotest.core.spec.style.FunSpec

            abstract class CalculatorBaseSpec(body: CalculatorBaseSpec.(Calculator) -> Unit) : FunSpec() {
                init {
                    body(Calculator())
                }
            }
            """,
        )
        project.file(
            "app/build.gradle.kts",
            kotlinJvmBuild(applyKotlin = false).replace(
                "dependencies {",
                "dependencies {\n        testImplementation(testFixtures(project(\":fixtures\")))",
            ),
        )
        val path = "app/src/test/kotlin/com/example/FixtureSpec.kt"
        project.file(
            path,
            """
            package com.example

            import com.example.fixtures.CalculatorBaseSpec

            class FixtureSpec : CalculatorBaseSpec({ calculator ->
                test("from fixtures") {
                    calculator.add(1, 1)
                }
            })
            """,
        )

        val result = project.build(project.discoverArguments(path, ":app") + "--info")

        // the test fixtures of `fixtures` and the production code of `core`
        result.output shouldContain "Discovering tests with 1 test, 0 main and 2 dependency source files"
        readJson(project.discoveryOutput(path)).discoveredTests() shouldContainExactly
            mapOf(
                "com.example.FixtureSpec" to "CONTAINER",
                "com.example.FixtureSpec::from fixtures" to "TEST",
            )
    }
})
