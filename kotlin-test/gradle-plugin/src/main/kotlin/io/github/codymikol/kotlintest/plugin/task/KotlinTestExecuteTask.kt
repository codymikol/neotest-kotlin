package io.github.codymikol.kotlintest.plugin.task

import io.github.codymikol.kotlintest.plugin.ExecutionCompatibility
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.objectweb.asm.ClassReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.streams.asSequence

abstract class KotlinTestExecuteTask : JavaExec() {
    companion object {
        /**
         * Main class of the `runner` jar.
         */
        const val MAIN = "io.github.codymikol.kotlintest.execute.RunnerKt"

        /**
         * Classpath resource of the `runner` jar embedded in this plugin, see `gradle-plugin/build.gradle.kts`.
         */
        const val RUNNER_JAR_RESOURCE = "/io/github/codymikol/kotlintest/runner/kotlin-test-runner.jar"
    }

    /**
     * Comma separated fully qualified class names or packages.
     *
     * ```
     * com.example.TestExample
     * com.example
     * ```
     */
    @get:Input
    abstract val classes: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val testSourceSetClasspath: Property<FileCollection>

    /**
     * `group:name` to version of every module on the test runtime classpath.
     */
    @get:Input
    abstract val testRuntimeModules: MapProperty<String, String>

    /**
     * Filter expects the format to be `::` separated and include
     * the fully qualified className.
     *
     * ```
     * org.example.TestExample::namespace::nested namespace::test
     *
     * org.example.TestExample::test
     *
     * org.example.TestExample::namespace
     * ```
     */
    @get:Optional
    @get:Input
    abstract val filter: Property<String>

    override fun exec() {
        val projectPath = path.substringBeforeLast(":").ifEmpty { ":" }
        ExecutionCompatibility
            .unsupportedKotestMessage(projectPath = projectPath, modules = testRuntimeModules.get())
            ?.let { throw GradleException(it) }

        val outputFile = this@KotlinTestExecuteTask.outputFile.asFile.get()
        val filter = this@KotlinTestExecuteTask.filter.orNull
        val classes =
            testSourceSetClasspath
                .get()
                .findMatchingClasses(classes = classes.get().split(","))
                .joinToString(separator = ",")

        if (classes.isEmpty()) {
            return
        }

        // The runner goes last, so the project's own test frameworks and libraries always win.
        classpath(extractRunnerJar())

        println("Executing: $MAIN --classes=$classes --output=$outputFile --filter=${filter.orEmpty()}")

        this.args(
            listOfNotNull(
                "--classes=$classes",
                "--output=$outputFile",
                filter?.let { "--filter=$it" },
            ),
        )

        super.exec()
    }

    /**
     * Extracts the runner jar embedded in this plugin, so it can be put on the classpath of the test JVM.
     */
    private fun extractRunnerJar(): File {
        val runnerJar = File(temporaryDir, "kotlin-test-runner.jar")
        val resource =
            checkNotNull(KotlinTestExecuteTask::class.java.getResourceAsStream(RUNNER_JAR_RESOURCE)) {
                "Could not find $RUNNER_JAR_RESOURCE in the kotlin-test Gradle plugin"
            }

        resource.use { Files.copy(it, runnerJar.toPath(), StandardCopyOption.REPLACE_EXISTING) }

        return runnerJar
    }
}

/**
 * Whether the [Path] is a Java Class and not a nested class.
 */
internal fun Path.isTopLevelClass(): Boolean = this.name.endsWith(".class") && "$" !in this.name

/**
 * Parses the Java Class located at [Path] getting its fully qualified class name.
 */
internal fun Path.toQualifiedClassName(): String =
    ClassReader(this.readBytes())
        .className
        .replace("/", ".")

/**
 * Determines all fully qualified class names in the [FileCollection] that match the [classes].
 */
internal fun FileCollection.findMatchingClasses(classes: List<String>): Set<String> =
    this
        .filter { it.exists() }
        .flatMap { file ->
            Files.walk(file.toPath()).asSequence().filter { path -> path.isTopLevelClass() }
        }.map { classPath -> classPath.toQualifiedClassName() }
        .filter { fqcn ->
            classes.any { className -> fqcn == className || fqcn.startsWith(className) }
        }.toSet()
