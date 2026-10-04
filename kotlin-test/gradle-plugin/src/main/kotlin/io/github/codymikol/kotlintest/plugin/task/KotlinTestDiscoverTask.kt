package io.github.codymikol.kotlintest.plugin.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintest.command.discover
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class KotlinTestDiscoverTask : SourceTask() {
    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
    }

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val includeFile: RegularFileProperty

    /**
     * Production sources (e.g. `src/main`) the tests can reference. They are only used
     * to resolve symbols (super classes, annotations, ...), tests are never discovered in them.
     */
    @get:InputFiles
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mainSources: ConfigurableFileCollection

    /**
     * Compile classpath of the tests (library jars), used to resolve symbols from dependencies.
     * When empty, discovery falls back to stubs of the Kotest and JUnit APIs.
     */
    @get:Classpath
    abstract val testCompileClasspath: ConfigurableFileCollection

    /**
     * Home of the JDK used to resolve JDK symbols.
     */
    @get:Input
    @get:Optional
    abstract val jdkHome: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val classpath = testCompileClasspath.files
        val jdkHome = jdkHome.orNull?.let(::File)
        logger.info(
            "Discovering tests with {} classpath entries and JDK {}",
            classpath.size,
            jdkHome,
        )

        val start = System.nanoTime()
        val results =
            discover(
                files = source.files,
                include = includeFile.get().asFile,
                mainFiles = mainSources.files,
                classpath = classpath,
                jdkHome = jdkHome,
            )
        logger.info("Discovered tests in {} ms", (System.nanoTime() - start) / NANOS_PER_MILLI)

        val objectMapper = ObjectMapper().registerKotlinModule()
        objectMapper.writeValue(outputFile.get().asFile, results)
    }
}
