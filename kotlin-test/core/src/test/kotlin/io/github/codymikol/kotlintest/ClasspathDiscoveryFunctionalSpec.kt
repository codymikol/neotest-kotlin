package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.command.discover
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import javax.tools.ToolProvider

/**
 * Library sources compiled into a jar, simulating a dependency (e.g. a shared test-fixtures
 * jar or an internal testing library) that is only available as a binary on the classpath.
 */
private val librarySources = mapOf(
    "com/example/lib/LibraryFunSpec.java" to """
        package com.example.lib;

        import io.kotest.core.spec.style.FunSpec;
        import kotlin.Unit;
        import kotlin.jvm.functions.Function1;

        public abstract class LibraryFunSpec extends FunSpec {
            protected LibraryFunSpec() {
                super(spec -> Unit.INSTANCE);
            }

            protected LibraryFunSpec(Function1<? super FunSpec, Unit> body) {
                super(body);
            }
        }
    """.trimIndent(),
    "com/example/lib/LibraryTest.java" to """
        package com.example.lib;

        import java.lang.annotation.ElementType;
        import java.lang.annotation.Retention;
        import java.lang.annotation.RetentionPolicy;
        import java.lang.annotation.Target;
        import org.junit.jupiter.api.Test;

        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @Test
        public @interface LibraryTest {}
    """.trimIndent(),
)

/**
 * Compiles [sources] against the current test classpath into a jar located in [directory].
 */
private fun compileJar(directory: File, sources: Map<String, String>): File {
    val sourceDir = directory.resolve("src")
    val classesDir = directory.resolve("classes").apply { mkdirs() }
    val sourceFiles = sources.map { (path, code) ->
        sourceDir.resolve(path).apply {
            parentFile.mkdirs()
            writeText(code)
        }
    }

    val compiler = ToolProvider.getSystemJavaCompiler().shouldNotBeNull()
    val exitCode = compiler.run(
        null,
        null,
        null,
        "-classpath",
        System.getProperty("java.class.path"),
        "-d",
        classesDir.absolutePath,
        *sourceFiles.map { it.absolutePath }.toTypedArray(),
    )
    exitCode shouldBe 0

    val jar = directory.resolve("library.jar")
    JarOutputStream(jar.outputStream()).use { output ->
        classesDir.walkTopDown().filter { it.isFile }.forEach { classFile ->
            output.putNextEntry(JarEntry(classFile.relativeTo(classesDir).invariantSeparatorsPath))
            classFile.inputStream().use { it.copyTo(output) }
            output.closeEntry()
        }
    }

    return jar
}

private val testClasspath: List<File> =
    System.getProperty("java.class.path")
        .split(File.pathSeparator)
        .filter { it.isNotBlank() }
        .map(::File)

private val jdkHome = File(System.getProperty("java.home"))

private fun Set<Discovered.Container>.ids(): List<String> =
    flatMap { container -> listOf(container.id) + container.tests.map { it.id } }

class ClasspathDiscoveryFunctionalSpec :
    FunSpec({
        val directory = tempdir()
        val libraryJar = compileJar(directory.resolve("library"), librarySources)

        fun write(path: String, code: String): File = directory
            .resolve(path)
            .apply {
                parentFile.mkdirs()
                writeText(code.trimIndent())
            }

        context("with the test classpath") {
            test("Kotest spec extending a base spec from a jar") {
                val spec = write(
                    "test/LibrarySubclassSpec.kt",
                    """
                    package org.example

                    import com.example.lib.LibraryFunSpec

                    class LibrarySubclassSpec : LibraryFunSpec() {
                        init {
                            test("from library base spec") { }
                        }
                    }
                    """
                )

                val result = discover(
                    files = listOf(spec),
                    classpath = testClasspath + libraryJar,
                    jdkHome = jdkHome,
                )

                result.warnings.shouldBeEmpty()
                result.tests.ids() shouldContainExactlyInAnyOrder listOf(
                    "org.example.LibrarySubclassSpec",
                    "org.example.LibrarySubclassSpec::from library base spec",
                )
            }

            test("Kotest spec extending a project base spec, extending a base spec from a jar") {
                val base = write(
                    "test/ProjectBaseSpec.kt",
                    """
                    package org.example

                    import com.example.lib.LibraryFunSpec

                    abstract class ProjectBaseSpec(body: ProjectBaseSpec.() -> Unit) : LibraryFunSpec() {
                        init {
                            body()
                        }
                    }
                    """
                )
                val spec = write(
                    "test/ProjectSubclassSpec.kt",
                    """
                    package org.example

                    class ProjectSubclassSpec : ProjectBaseSpec({
                        context("container") {
                            test("nested") { }
                        }
                    })
                    """
                )

                val result = discover(
                    files = listOf(base, spec),
                    include = spec,
                    classpath = testClasspath + libraryJar,
                    jdkHome = jdkHome,
                )

                result.tests.ids() shouldContainExactlyInAnyOrder listOf(
                    "org.example.ProjectSubclassSpec",
                    "org.example.ProjectSubclassSpec::container",
                )
                result.tests
                    .single()
                    .tests
                    .single()
                    .shouldBeContainerWith("org.example.ProjectSubclassSpec::container::nested")
            }

            test("Kotest spec extending a base spec from main sources") {
                val base = write(
                    "main/MainBaseSpec.kt",
                    """
                    package org.example

                    import io.kotest.core.spec.style.DescribeSpec

                    abstract class MainBaseSpec(body: MainBaseSpec.() -> Unit) : DescribeSpec() {
                        init {
                            body()
                        }
                    }
                    """
                )
                val spec = write(
                    "test/MainSubclassSpec.kt",
                    """
                    package org.example

                    class MainSubclassSpec : MainBaseSpec({
                        it("from main base spec") { }
                    })
                    """
                )

                val result = discover(
                    files = listOf(spec),
                    mainFiles = listOf(base),
                    classpath = testClasspath,
                    jdkHome = jdkHome,
                )

                result.tests.ids() shouldContainExactlyInAnyOrder listOf(
                    "org.example.MainSubclassSpec",
                    "org.example.MainSubclassSpec::from main base spec",
                )
            }

            test("main sources are only used for resolution, never discovered") {
                val mainSpec = write(
                    "main/NotATestSpec.kt",
                    """
                    package org.example

                    import io.kotest.core.spec.style.FunSpec

                    class NotATestSpec : FunSpec({
                        test("not discovered") { }
                    })
                    """
                )
                val spec = write(
                    "test/EmptyFile.kt",
                    """
                    package org.example
                    """
                )

                val result = discover(
                    files = listOf(spec),
                    mainFiles = listOf(mainSpec),
                    classpath = testClasspath,
                    jdkHome = jdkHome,
                )

                result.tests.shouldBeEmpty()
            }

            test("JUnit test using a meta-annotation from a jar") {
                val spec = write(
                    "test/LibraryAnnotationTest.kt",
                    """
                    package org.example

                    import com.example.lib.LibraryTest

                    class LibraryAnnotationTest {
                        @LibraryTest
                        fun libraryAnnotated() { }

                        fun notATest() { }
                    }
                    """
                )

                val result = discover(
                    files = listOf(spec),
                    classpath = testClasspath + libraryJar,
                    jdkHome = jdkHome,
                )

                result.tests.ids() shouldContainExactlyInAnyOrder listOf(
                    "org.example.LibraryAnnotationTest",
                    "org.example.LibraryAnnotationTest::libraryAnnotated",
                )
            }

            test("JUnit test using a JUnit meta-annotation") {
                val spec = write(
                    "test/RepeatedJUnitTest.kt",
                    """
                    package org.example

                    import org.junit.jupiter.api.DisplayName
                    import org.junit.jupiter.api.Disabled
                    import org.junit.jupiter.api.RepeatedTest
                    import org.junit.jupiter.api.Test

                    class RepeatedJUnitTest {
                        @RepeatedTest(2)
                        @DisplayName("repeated display name")
                        fun repeated() { }

                        @Test
                        @Disabled
                        fun disabled() { }
                    }
                    """
                )

                val result = discover(
                    files = listOf(spec),
                    classpath = testClasspath,
                    jdkHome = jdkHome,
                )

                result.tests.ids() shouldContainExactlyInAnyOrder listOf(
                    "org.example.RepeatedJUnitTest",
                    "org.example.RepeatedJUnitTest::repeated display name",
                )
            }
        }

        context("without a classpath (stub fallback)") {
            test("Kotest spec extending a base spec from a jar is not resolvable") {
                val spec = write(
                    "stub/LibrarySubclassSpec.kt",
                    """
                    package org.example

                    import com.example.lib.LibraryFunSpec

                    class LibrarySubclassSpec : LibraryFunSpec() {
                        init {
                            test("from library base spec") { }
                        }
                    }
                    """
                )

                discover(files = listOf(spec)).tests.shouldBeEmpty()
            }

            test("JUnit meta-annotation from a jar is not resolvable") {
                val spec = write(
                    "stub/LibraryAnnotationTest.kt",
                    """
                    package org.example

                    import com.example.lib.LibraryTest

                    class LibraryAnnotationTest {
                        @LibraryTest
                        fun libraryAnnotated() { }
                    }
                    """
                )

                discover(files = listOf(spec)).tests.shouldBeEmpty()
            }

            test("Kotest spec extending FunSpec resolves against the stubs") {
                val spec = write(
                    "stub/StubFunSpec.kt",
                    """
                    package org.example

                    import io.kotest.core.spec.style.FunSpec

                    class StubFunSpec : FunSpec({
                        test("stub") { }
                    })
                    """
                )

                discover(files = listOf(spec)).tests.ids() shouldContainExactlyInAnyOrder listOf(
                    "org.example.StubFunSpec",
                    "org.example.StubFunSpec::stub",
                )
            }
        }
    })

private fun Discovered.shouldBeContainerWith(vararg ids: String) {
    val container = this as? Discovered.Container
    container.shouldNotBeNull()
    container.tests.map { it.id } shouldContainExactlyInAnyOrder ids.toList()
}
