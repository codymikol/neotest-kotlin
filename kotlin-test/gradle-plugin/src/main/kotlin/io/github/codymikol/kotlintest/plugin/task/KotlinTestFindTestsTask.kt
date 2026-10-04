package io.github.codymikol.kotlintest.plugin.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintest.command.findTestFiles
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Determines which of the [source] files contain tests, writing their absolute paths as JSON
 * (`{"testFiles": [...]}`) to [outputFile].
 *
 * A file contains tests when discovery finds at least one test class or spec in it, symbols are
 * resolved the same way as by [KotlinTestDiscoverTask].
 */
@CacheableTask
abstract class KotlinTestFindTestsTask : SourceTask() {
    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
    }

    /**
     * Production sources (e.g. `src/main`) the tests can reference. They are only used
     * to resolve symbols (super classes, annotations, ...), they are never test files.
     */
    @get:InputFiles
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mainSources: ConfigurableFileCollection

    /**
     * Sources of other projects the tests depend on (e.g. `main` or `testFixtures` of a project dependency).
     * Like [mainSources], they are only used to resolve symbols.
     */
    @get:InputFiles
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dependencySources: ConfigurableFileCollection

    /**
     * Compile classpath of the tests (library jars), used to resolve symbols from dependencies.
     * When empty, stubs of the Kotest and JUnit APIs are used instead.
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
        val files = source.files
        val mainFiles = mainSources.files - files
        // e.g. this project's own sources, reachable through its test fixtures
        val dependencyFiles = dependencySources.files - files - mainFiles

        val start = System.nanoTime()
        val result =
            findTestFiles(
                files = files,
                mainFiles = mainFiles,
                classpath = testCompileClasspath.files,
                jdkHome = jdkHome.orNull?.let(::File),
                dependencyFiles = dependencyFiles,
            )
        logger.info(
            "Found {} test files out of {} files in {} ms",
            result.testFiles.size,
            files.size,
            (System.nanoTime() - start) / NANOS_PER_MILLI,
        )

        ObjectMapper().registerKotlinModule().writeValue(outputFile.get().asFile, result)
    }
}
