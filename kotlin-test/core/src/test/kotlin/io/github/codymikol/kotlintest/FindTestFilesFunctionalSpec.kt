package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.command.discover
import io.github.codymikol.kotlintest.command.findTestFiles
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldBeSorted
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.io.File

private val testClasspath: List<File> =
    System.getProperty("java.class.path")
        .split(File.pathSeparator)
        .filter { it.isNotBlank() }
        .map(::File)

private val jdkHome = File(System.getProperty("java.home"))

/**
 * Test sources of a project, keyed by their path relative to the test source directory.
 */
private val testSources = mapOf(
    // not test files
    "Helpers.kt" to """
        package org.example

        fun <T> retry(times: Int, block: () -> T): T = block()

        class Clock {
            fun now(): Long = 0
        }
    """,
    "fixtures/PersonFixture.kt" to """
        package org.example.fixtures

        data class Person(val name: String, val age: Int)

        object PersonFixture {
            fun aPerson(name: String = "John"): Person = Person(name, 42)
        }
    """,
    "io/kotest/provided/ProjectConfig.kt" to """
        package io.kotest.provided

        import io.kotest.core.config.AbstractProjectConfig

        object ProjectConfig : AbstractProjectConfig()
    """,
    "BaseSpec.kt" to """
        package org.example

        import io.kotest.core.spec.style.FunSpec

        abstract class BaseSpec(body: BaseSpec.() -> Unit) : FunSpec() {
            init {
                body()
            }
        }
    """,
    "IntegrationTest.kt" to """
        package org.example

        import org.junit.jupiter.api.Test

        @Target(AnnotationTarget.FUNCTION)
        @Retention(AnnotationRetention.RUNTIME)
        @Test
        annotation class IntegrationTest
    """,
    "DisabledJUnitTest.kt" to """
        package org.example

        import org.junit.jupiter.api.Disabled
        import org.junit.jupiter.api.Test

        @Disabled
        class DisabledJUnitTest {
            @Test
            fun test() = Unit
        }
    """,
    // test files
    "ExampleFunSpec.kt" to """
        package org.example

        import io.kotest.core.spec.style.FunSpec

        class ExampleFunSpec : FunSpec({
            test("test") { }
        })
    """,
    "ObjectStringSpec.kt" to """
        package org.example

        import io.kotest.core.spec.style.StringSpec

        object ObjectStringSpec : StringSpec({
            "test" { }
        })
    """,
    "SubclassSpec.kt" to """
        package org.example

        class SubclassSpec : BaseSpec({
            test("test") { }
        })
    """,
    "MainSubclassSpec.kt" to """
        package org.example

        import org.example.main.MainBaseSpec

        class MainSubclassSpec : MainBaseSpec({
            it("test") { }
        })
    """,
    "ExampleJUnitTest.kt" to """
        package org.example

        import org.junit.jupiter.api.Test

        class ExampleJUnitTest {
            @Test
            fun test() = Unit
        }
    """,
    "MetaAnnotatedJUnitTest.kt" to """
        package org.example

        class MetaAnnotatedJUnitTest {
            @IntegrationTest
            fun test() = Unit
        }
    """,
    "ExampleKotlinTest.kt" to """
        package org.example

        import kotlin.test.Test

        class ExampleKotlinTest {
            @Test
            fun test() = Unit
        }
    """,
)

private val mainSources = mapOf(
    "org/example/main/MainBaseSpec.kt" to """
        package org.example.main

        import io.kotest.core.spec.style.DescribeSpec

        abstract class MainBaseSpec(body: MainBaseSpec.() -> Unit) : DescribeSpec() {
            init {
                body()
            }
        }
    """,
)

private val expectedTestFiles = listOf(
    "ExampleFunSpec.kt",
    "ObjectStringSpec.kt",
    "SubclassSpec.kt",
    "MainSubclassSpec.kt",
    "ExampleJUnitTest.kt",
    "MetaAnnotatedJUnitTest.kt",
    "ExampleKotlinTest.kt",
)

/**
 * Like [ClasspathDiscoveryFunctionalSpec], this spec uses real (non unit test mode) analysis sessions
 * which can't be disposed once a unit test mode session was created in the same JVM. It lives in this
 * package so it runs before the specs using unit test mode sessions (specs run in lexicographic order).
 */
class FindTestFilesFunctionalSpec : FunSpec({
    val directory = tempdir()

    fun write(sources: Map<String, String>, root: File): Map<String, File> =
        sources.mapValues { (path, code) ->
            root.resolve(path).apply {
                parentFile.mkdirs()
                writeText(code.trimIndent())
            }
        }

    val testFiles = write(testSources, directory.resolve("src/test/kotlin"))
    val mainFiles = write(mainSources, directory.resolve("src/main/kotlin"))

    // the analysis must run in a test, not while instantiating the spec
    val result by lazy {
        findTestFiles(
            files = testFiles.values,
            mainFiles = mainFiles.values,
            classpath = testClasspath,
            jdkHome = jdkHome,
        )
    }

    test("only files containing tests are test files") {
        result.testFiles shouldContainExactlyInAnyOrder expectedTestFiles.map { testFiles.getValue(it).absolutePath }
    }

    test("test files are sorted") {
        result.testFiles.shouldBeSorted()
    }

    test("test files are the files in which discovery finds tests") {
        val discovered = testFiles.values
            .filter { file ->
                discover(
                    files = testFiles.values,
                    include = file,
                    mainFiles = mainFiles.values,
                    classpath = testClasspath,
                    jdkHome = jdkHome,
                ).tests.isNotEmpty()
            }
            .map { it.absolutePath }

        result.testFiles shouldContainExactlyInAnyOrder discovered
    }

    test("stubs are used without a classpath") {
        val files = listOf("ExampleFunSpec.kt", "Helpers.kt").map(testFiles::getValue)

        val stubResult = findTestFiles(files = files)

        stubResult.testFiles shouldBe listOf(testFiles.getValue("ExampleFunSpec.kt").absolutePath)
    }
})
