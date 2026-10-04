package io.github.codymikol.kotlintest.plugin

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.TaskOutcome

/**
 * Discovery and execution of a single project Kotlin build with Kotest and JUnit Jupiter tests.
 *
 * The tests share one generated project (and therefore its Gradle and build caches) to keep the runtime down.
 */
class DiscoveryAndExecutionSpec : FunSpec({
    val project = FunctionalTestProject(tempdir())
    project.settings()
    project.calculatorProject()

    val discoverSpec = project.discoverArguments(CALCULATOR_SPEC_PATH)
    val discoverTest = project.discoverArguments(CALCULATOR_TEST_PATH)
    val executionOutput = project.dir.resolve("build/execution.json")
    val executeArguments =
        listOf(
            "kotlinTestExecute",
            "-Pclasses=com.example.CalculatorSpec,com.example.CalculatorTest",
            "-PoutputFile=${executionOutput.absolutePath}",
            "--configuration-cache",
        )

    test("discovery writes the tests of a Kotest spec") {
        project.build(discoverSpec)

        readJson(project.discoveryOutput(CALCULATOR_SPEC_PATH)).discoveredTests() shouldContainExactly
            mapOf(
                "com.example.CalculatorSpec" to "CONTAINER",
                "com.example.CalculatorSpec::add" to "CONTAINER",
                "com.example.CalculatorSpec::add::adds two numbers" to "TEST",
                "com.example.CalculatorSpec::add::fails on purpose" to "TEST",
            )
    }

    test("discovery writes the tests of a JUnit class") {
        project.build(discoverTest)

        readJson(project.discoveryOutput(CALCULATOR_TEST_PATH)).discoveredTests() shouldContainExactly
            mapOf(
                "com.example.CalculatorTest" to "CONTAINER",
                "com.example.CalculatorTest::addsTwoNumbers" to "TEST",
                "com.example.CalculatorTest::failsOnPurpose" to "TEST",
            )
    }

    test("discovery reuses the configuration cache and is up to date") {
        project.build(discoverSpec)
        val result = project.build(discoverSpec)

        result.output shouldContain "Reusing configuration cache."
        result.task(":kotlinTestDiscover")?.outcome shouldBe TaskOutcome.UP_TO_DATE
    }

    test("discovering another file reuses the configuration cache") {
        project.build(discoverSpec)
        val result = project.build(discoverTest)

        result.output shouldContain "Reusing configuration cache."
        result.task(":kotlinTestDiscover")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    test("adding a file reuses the configuration cache") {
        val path = "src/test/kotlin/com/example/AddedSpec.kt"
        project.build(discoverSpec)
        project.file(path, CALCULATOR_SPEC.replace("class CalculatorSpec", "class AddedSpec"))

        try {
            val result = project.build(project.discoverArguments(path))

            result.output shouldContain "Reusing configuration cache."
            readJson(project.discoveryOutput(path)).discoveredTests().keys shouldContain
                "com.example.AddedSpec::add::adds two numbers"
        } finally {
            project.dir.resolve(path).delete()
        }
    }

    test("discovery output is restored from the build cache") {
        val output = project.discoveryOutput(CALCULATOR_SPEC_PATH)

        output.delete()
        project.build(discoverSpec + "--build-cache")
        val expected = output.readText()

        output.delete()
        val result = project.build(discoverSpec + "--build-cache")

        result.task(":kotlinTestDiscover")?.outcome shouldBe TaskOutcome.FROM_CACHE
        output.readText() shouldBe expected
    }

    // The Kotlin Gradle plugin is applied by the build, its classes aren't visible to the plugin loaded
    // by the init script, so this also fails if the plugin references them (e.g. KotlinCompile).
    test("kotlinTestExecute writes the results of passed and failed tests") {
        executionOutput.delete()

        val result = project.build(executeArguments)

        result.task(":kotlinTestExecute")?.outcome shouldBe TaskOutcome.SUCCESS
        readJson(executionOutput).executionResults() shouldContainExactly
            mapOf(
                "com.example.CalculatorSpec::add::adds two numbers" to "SUCCESS",
                "com.example.CalculatorSpec::add::fails on purpose" to "FAILURE",
                "com.example.CalculatorTest::addsTwoNumbers" to "SUCCESS",
                "com.example.CalculatorTest::failsOnPurpose" to "FAILURE",
            )
    }

    test("kotlinTestExecute reuses the configuration cache") {
        project.build(executeArguments)
        executionOutput.delete()

        val result = project.build(executeArguments)

        result.output shouldContain "Reusing configuration cache."
        result.task(":kotlinTestExecute")?.outcome shouldBe TaskOutcome.SUCCESS
        readJson(executionOutput).executionResults().keys shouldContainExactlyInAnyOrder
            setOf(
                "com.example.CalculatorSpec::add::adds two numbers",
                "com.example.CalculatorSpec::add::fails on purpose",
                "com.example.CalculatorTest::addsTwoNumbers",
                "com.example.CalculatorTest::failsOnPurpose",
            )
    }

    test("kotlinTestExecute runs only the tests matching the filter") {
        executionOutput.delete()

        project.build(
            "kotlinTestExecute",
            "-Pclasses=com.example.CalculatorSpec",
            "-Pfilter=com.example.CalculatorSpec::add::adds two numbers",
            "-PoutputFile=${executionOutput.absolutePath}",
            "--configuration-cache",
        )

        // Kotest reports the tests excluded by the filter as ignored
        val results = readJson(executionOutput).executionResults()
        results["com.example.CalculatorSpec::add::adds two numbers"] shouldBe "SUCCESS"
        results.values shouldNotContain "FAILURE"
    }
})
