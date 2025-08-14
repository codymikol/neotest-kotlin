package io.github.codymikol.kotlintest.plugin.task

import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.objectweb.asm.ClassReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.streams.asSequence

abstract class KotlinTestTask : JavaExec() {
    companion object {
        const val MAIN = "io.github.codymikol.kotlintest.MainKt"
    }

    @get:Input
    abstract val classes: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val testSourceSetClasspath: Property<FileCollection>

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
     * Loads all classes in the [FileCollection] that match the [requestedClasses].
     */
    internal fun FileCollection.loadClasses(requestedClasses: List<String>): Set<String> =
        this
            .filter { it.exists() }
            .flatMap { file ->
                Files.walk(file.toPath()).asSequence().filter { path -> path.isTopLevelClass() }
            }.map { classPath -> classPath.toQualifiedClassName() }
            .filter { fqcn ->
                requestedClasses.any { className -> fqcn == className || fqcn.startsWith(className) }
            }.toSet()

    override fun exec() {
        val outputFile = this@KotlinTestTask.outputFile.asFile.get()
        val filter = this@KotlinTestTask.filter.orNull
        val classes =
            testSourceSetClasspath
                .get()
                .loadClasses(requestedClasses = classes.get().split(",")).joinToString(separator = ",")

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
}
