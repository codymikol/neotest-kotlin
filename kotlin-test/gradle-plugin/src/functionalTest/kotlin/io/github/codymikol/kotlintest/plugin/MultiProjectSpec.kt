package io.github.codymikol.kotlintest.plugin

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.TaskOutcome

private const val LIB_SPEC_PATH = "lib/src/test/kotlin/com/example/LibSpec.kt"
private const val APP_SHARED_SPEC_PATH = "app/src/test/kotlin/com/example/SharedSpec.kt"
private const val LIB_SHARED_SPEC_PATH = "lib/src/test/kotlin/com/example/SharedSpec.kt"

/**
 * A spec with the same fully qualified name in several projects, with a test named after the project.
 */
private fun sharedSpec(project: String) =
    """
    package com.example

    import io.kotest.core.spec.style.FunSpec

    class SharedSpec : FunSpec({
        test("from $project") { }
    })
    """

/**
 * A build whose root project declares the Kotlin plugin without applying it, like the example project.
 * The init script applies the plugin to the root project and to every subproject.
 *
 * `app` depends on `lib`, both have tests, and both have a `com.example.SharedSpec`.
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
    project.file("lib/build.gradle.kts", kotlinJvmBuild(applyKotlin = false))
    project.file("lib/src/main/kotlin/com/example/Calculator.kt", CALCULATOR)
    project.file(
        LIB_SPEC_PATH,
        """
        package com.example

        import io.kotest.core.spec.style.FunSpec
        import io.kotest.matchers.shouldBe

        class LibSpec : FunSpec({
            test("adds in lib") {
                Calculator().add(1, 1) shouldBe 2
            }
        })
        """,
    )
    project.file(LIB_SHARED_SPEC_PATH, sharedSpec("lib"))
    project.file(
        "app/build.gradle.kts",
        kotlinJvmBuild(applyKotlin = false).replace(
            "dependencies {",
            "dependencies {\n        testImplementation(project(\":lib\"))",
        ),
    )
    project.file("app/$CALCULATOR_SPEC_PATH", CALCULATOR_SPEC)
    project.file(APP_SHARED_SPEC_PATH, sharedSpec("app"))

    test("tasks are registered once per Kotlin project") {
        val result = project.build("tasks", "--all")

        listOf("kotlinTestExecute", "kotlinTestDiscover", "kotlinTestFindTests").forEach { task ->
            Regex("""^(\S+:)?$task\b""", RegexOption.MULTILINE)
                .findAll(result.output)
                .map { it.value }
                .toList() shouldBe listOf("app:$task", "lib:$task")
        }
    }

    test("discovery resolves sources of other projects") {
        val path = "app/$CALCULATOR_SPEC_PATH"
        val result = project.build(project.discoverArguments(path, ":app") + "--info")

        result.output shouldContain "Configuration cache entry stored."
        // the sources of `app` and `lib`, never the tests of `lib`
        result.output shouldContain "Discovering tests with 2 test, 0 main and 1 dependency source files"
        val discovered = readJson(project.discoveryOutput(path)).discoveredTests()
        discovered.filterValues { it == "TEST" } shouldContainExactly
            mapOf(
                "com.example.CalculatorSpec::add::adds two numbers" to "TEST",
                "com.example.CalculatorSpec::add::fails on purpose" to "TEST",
            )
    }

    test("discovery of a project only uses the sources of that project") {
        // with the sources of every project, both SharedSpec would be part of the same session
        listOf(":app" to APP_SHARED_SPEC_PATH, ":lib" to LIB_SHARED_SPEC_PATH).forEach { (projectPath, path) ->
            project.build(project.discoverArguments(path, projectPath))

            readJson(project.discoveryOutput(path)).discoveredTests() shouldContainExactly
                mapOf(
                    "com.example.SharedSpec" to "CONTAINER",
                    "com.example.SharedSpec::from ${projectPath.removePrefix(":")}" to "TEST",
                )
        }
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
        result.task(":lib:kotlinTestExecute") shouldBe null
        readJson(output).executionResults() shouldContainExactly
            mapOf(
                "com.example.CalculatorSpec::add::adds two numbers" to "SUCCESS",
                "com.example.CalculatorSpec::add::fails on purpose" to "FAILURE",
            )
    }

    test("kotlinTestExecute of several projects writes the results of each project") {
        val outputDir = project.dir.resolve("build/execution")

        val result =
            project.build(
                ":app:kotlinTestExecute",
                ":lib:kotlinTestExecute",
                "-Pclasses=com.example.SharedSpec,com.example.LibSpec",
                "-PoutputDir=${outputDir.absolutePath}",
            )

        result.task(":app:kotlinTestExecute")?.outcome shouldBe TaskOutcome.SUCCESS
        result.task(":lib:kotlinTestExecute")?.outcome shouldBe TaskOutcome.SUCCESS
        readJson(outputDir.resolve("_app.json")).executionResults() shouldContainExactly
            mapOf("com.example.SharedSpec::from app" to "SUCCESS")
        readJson(outputDir.resolve("_lib.json")).executionResults() shouldContainExactly
            mapOf(
                "com.example.SharedSpec::from lib" to "SUCCESS",
                "com.example.LibSpec::adds in lib" to "SUCCESS",
            )
    }

    test("kotlinTestFindTests writes the test files of each project") {
        val result = project.build("kotlinTestFindTests", "--configuration-cache")

        result.task(":app:kotlinTestFindTests")?.outcome shouldBe TaskOutcome.SUCCESS
        result.task(":lib:kotlinTestFindTests")?.outcome shouldBe TaskOutcome.SUCCESS

        val outputDir = project.dir.resolve("build/kotlinTestFindTests")
        fun testFiles(name: String) = readJson(outputDir.resolve(name)).get("testFiles").map { it.asText() }

        testFiles("_app.json") shouldContainExactlyInAnyOrder
            listOf("app/$CALCULATOR_SPEC_PATH", APP_SHARED_SPEC_PATH).map { project.dir.resolve(it).absolutePath }
        testFiles("_lib.json") shouldContainExactlyInAnyOrder
            listOf(LIB_SPEC_PATH, LIB_SHARED_SPEC_PATH).map { project.dir.resolve(it).absolutePath }
    }
})
