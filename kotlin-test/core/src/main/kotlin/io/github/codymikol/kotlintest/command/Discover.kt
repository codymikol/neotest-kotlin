package io.github.codymikol.kotlintest.command

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.DiscoveredResult
import io.github.codymikol.kotlintest.provider.Analysis
import io.github.codymikol.kotlintest.provider.AnalysisApiSession
import java.io.File
import kotlin.system.exitProcess

public class Discover : CliktCommand() {
    /**
     * All files that exist in the project that could contain tests.
     */
    private val files: List<File> by option(
        help = "Comma separated absolute paths to files for discovery"
    ).file(canBeDir = false).split(",").required()

    /**
     * Files that are being specifically discovered. This is different from [files] to
     * allow for more specific discovery while still determining symbols/parent classes
     * across other files.
     */
    private val includeFiles: List<File>? by option(
        help = "Comma separated absolute paths to files for discovery to only include"
    ).file(canBeDir = false).split(",")

    /**
     * Production source files the tests depend on, used only to resolve symbols.
     */
    private val mainFiles: List<File> by option(
        help = "Comma separated absolute paths to production source files used to resolve symbols"
    ).file(canBeDir = false).split(",").default(emptyList())

    /**
     * Sources of other projects the tests depend on, used only to resolve symbols.
     */
    private val dependencyFiles: List<File> by option(
        help = "Comma separated absolute paths to source files of project dependencies used to resolve symbols"
    ).file(canBeDir = false).split(",").default(emptyList())

    /**
     * Test compile classpath, used to resolve symbols from libraries.
     */
    private val classpath: List<File> by option(
        help = "Test compile classpath entries separated by '${File.pathSeparator}'"
    ).file().split(File.pathSeparator).default(emptyList())

    private val jdkHome: File? by option(help = "JDK home used to resolve JDK symbols").file(canBeFile = false)

    private val output: File by option(help = "File to write the JSON test results").file().required()

    override fun run() {
        val mapper = ObjectMapper().registerKotlinModule()

        val result = AnalysisApiSession(
            files = files.map { Analysis.File(it) },
            mainFiles = mainFiles.map { Analysis.File(it) },
            dependencyFiles = dependencyFiles.map { Analysis.File(it) },
            classpath = classpath,
            jdkHome = jdkHome,
        ).use { session ->
            val filesToDiscover = session.kotlinFiles
                .filter { kotlinFile ->
                    includeFiles == null || includeFiles
                        ?.map { it.absolutePath }
                        ?.contains(kotlinFile.virtualFilePath) == true
                }.toSet()

            TestDiscoverer.discoverAllTests(filesToDiscover)
        }

        mapper.writeValue(output, result)
        exitProcess(0) // hangs forever otherwise, but we're done?
    }
}

/**
 * Performs Kotlin Analysis discovery using [files] as all dependencies
 * and [include] as the [File] to perform discovery on specifically. If [include]
 * is null discovery will be performed on all tests.
 *
 * Symbols are resolved against [mainFiles] (production sources the tests depend on),
 * [dependencyFiles] (sources of other projects the tests or [mainFiles] depend on),
 * [classpath] (library jars/class directories of the tests) and the JDK at [jdkHome].
 * When [classpath] is empty, stubs of the Kotest and JUnit APIs are used instead.
 */
@Suppress("LongParameterList") // all but files are optional
public fun discover(
    files: Collection<File>,
    include: File? = null,
    mainFiles: Collection<File> = emptyList(),
    classpath: Collection<File> = emptyList(),
    jdkHome: File? = null,
    dependencyFiles: Collection<File> = emptyList(),
): DiscoveredResult = AnalysisApiSession(
    files = files.map { Analysis.File(it) },
    mainFiles = mainFiles.map { Analysis.File(it) },
    dependencyFiles = dependencyFiles.map { Analysis.File(it) },
    classpath = classpath,
    jdkHome = jdkHome,
).use { session ->
    val filesToDiscover = session.kotlinFiles
        .filter { kotlinFile ->
            include == null || include.absolutePath == kotlinFile.virtualFilePath
        }.toSet()

    TestDiscoverer.discoverAllTests(filesToDiscover)
}
