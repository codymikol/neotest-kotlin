package io.github.codymikol.kotlintest.plugin.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintest.TestFrameworkRunner
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.reflect.KClass
import kotlin.streams.asSequence

abstract class KotlinTestTask : DefaultTask() {
    @get:Input
    abstract val classes: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val testSourceSetClasspath: Property<FileCollection>

    /**
     * Whether the [Path] is a Java Class and not a nested class.
     */
    internal fun Path.isClass(): Boolean = this.name.endsWith(".class") && "$" !in this.name

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
    internal fun FileCollection.loadClasses(
        classLoader: URLClassLoader,
        requestedClasses: List<String>,
    ): Set<KClass<*>> =
        this
            .filter { it.exists() }
            .flatMap { file ->
                Files.walk(file.toPath()).asSequence().filter { path -> path.isClass() }
            }.map { classPath -> classPath.toQualifiedClassName() }
            .filter { fqcn ->
                requestedClasses.any { className -> fqcn == className || fqcn.startsWith(className) }
            }.mapNotNull { fqcn ->
                try {
                    classLoader.loadClass(fqcn).kotlin
                } catch (_: ClassNotFoundException) {
                    null
                }
            }.toSet()

    @TaskAction
    fun run() {
        val outputFile = this@KotlinTestTask.outputFile.asFile.get()
        val testSourceSet = testSourceSetClasspath.get()
        val classLoader =
            URLClassLoader(testSourceSet.map { it.toURI().toURL() }.toTypedArray(), this.javaClass.classLoader)

        val classes =
            testSourceSet
                .loadClasses(classLoader = classLoader, requestedClasses = classes.get().split(","))

        val report = TestFrameworkRunner.runAll(classes = classes)
        val mapper = ObjectMapper().registerKotlinModule()

        mapper.writeValue(outputFile, report)
    }
}
