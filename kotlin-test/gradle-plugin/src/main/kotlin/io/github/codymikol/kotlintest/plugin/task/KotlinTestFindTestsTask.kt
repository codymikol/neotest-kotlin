package io.github.codymikol.kotlintest.plugin.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintest.command.discoverTestFiles
import io.github.codymikol.kotlintest.files.model.TestFilesResult
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
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
 *
 * The tests discovered in every test file are written to [discoveredDirectory] in the same run, see
 * [discoveredFile], each the same JSON [KotlinTestDiscoverTask] writes for that file. A whole project
 * is therefore discovered with a single analysis session instead of one per file.
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

    /**
     * Directory the tests discovered in each test file are written to, see [discoveredFile].
     */
    @get:OutputDirectory
    abstract val discoveredDirectory: DirectoryProperty

    @TaskAction
    fun execute() {
        val files = source.files
        val mainFiles = mainSources.files - files
        // e.g. this project's own sources, reachable through its test fixtures
        val dependencyFiles = dependencySources.files - files - mainFiles

        val start = System.nanoTime()
        val discovered =
            discoverTestFiles(
                files = files,
                mainFiles = mainFiles,
                classpath = testCompileClasspath.files,
                jdkHome = jdkHome.orNull?.let(::File),
                dependencyFiles = dependencyFiles,
            )
        logger.info(
            "Found {} test files out of {} files in {} ms",
            discovered.size,
            files.size,
            (System.nanoTime() - start) / NANOS_PER_MILLI,
        )

        val objectMapper = ObjectMapper().registerKotlinModule()

        // the results of files that are gone or no longer contain tests must not remain
        val directory = discoveredDirectory.get().asFile
        directory.deleteRecursively()
        discovered.forEach { (path, result) ->
            val file = discoveredFile(directory, path)
            file.parentFile.mkdirs()
            objectMapper.writeValue(file, result)
        }

        objectMapper.writeValue(outputFile.get().asFile, TestFilesResult(testFiles = discovered.keys.toList()))
    }
}

/**
 * The file in [directory] containing the tests discovered in the test file at the absolute [path]: the path below
 * [directory] followed by `.json`, e.g. `<directory>/home/me/project/src/test/kotlin/Spec.kt.json` for
 * `/home/me/project/src/test/kotlin/Spec.kt`.
 */
internal fun discoveredFile(directory: File, path: String): File =
    // e.g. `C:\` on Windows, which would resolve to the path itself
    directory.resolve(path.replace(":", "").trimStart('/', '\\') + ".json")
