package io.github.codymikol.kotlintest.command

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import io.github.codymikol.kotlintest.files.findTestFiles
import io.github.codymikol.kotlintest.files.model.TestFilesResult
import io.github.codymikol.kotlintest.provider.Analysis
import io.github.codymikol.kotlintest.provider.AnalysisApiSession
import java.io.File
import kotlin.system.exitProcess

/**
 * Determines which files contain tests, writing a [TestFilesResult] as JSON to [output].
 */
public class FindTestFiles : CliktCommand(name = "files") {
    /**
     * All files that exist in the project that could contain tests.
     */
    private val files: List<File> by option(
        help = "Comma separated absolute paths to files that could contain tests"
    ).file(canBeDir = false).split(",").required()

    /**
     * Production source files the tests depend on, used only to resolve symbols.
     */
    private val mainFiles: List<File> by option(
        help = "Comma separated absolute paths to production source files used to resolve symbols"
    ).file(canBeDir = false).split(",").default(emptyList())

    /**
     * Test compile classpath, used to resolve symbols from libraries.
     */
    private val classpath: List<File> by option(
        help = "Test compile classpath entries separated by '${File.pathSeparator}'"
    ).file().split(File.pathSeparator).default(emptyList())

    private val jdkHome: File? by option(help = "JDK home used to resolve JDK symbols").file(canBeFile = false)

    private val output: File by option(help = "File to write the JSON list of test files").file().required()

    override fun run() {
        val result = findTestFiles(
            files = files,
            mainFiles = mainFiles,
            classpath = classpath,
            jdkHome = jdkHome,
        )

        ObjectMapper().registerKotlinModule().writeValue(output, result)
        exitProcess(0) // the analysis session keeps non-daemon threads alive otherwise
    }
}

/**
 * Determines which of [files] contain tests, i.e. the files in which discovery finds at least
 * one test class or spec.
 *
 * Uses the same Kotlin Analysis session as [discover]: symbols are resolved against [mainFiles]
 * (production sources the tests depend on), [dependencyFiles] (sources of other projects the tests
 * depend on), [classpath] (library jars/class directories of the tests) and the JDK at [jdkHome].
 * When [classpath] is empty, stubs of the Kotest and JUnit APIs are used instead.
 */
public fun findTestFiles(
    files: Collection<File>,
    mainFiles: Collection<File> = emptyList(),
    classpath: Collection<File> = emptyList(),
    jdkHome: File? = null,
    dependencyFiles: Collection<File> = emptyList(),
): TestFilesResult = AnalysisApiSession(
    files = files.map { Analysis.File(it) },
    mainFiles = mainFiles.map { Analysis.File(it) },
    dependencyFiles = dependencyFiles.map { Analysis.File(it) },
    classpath = classpath,
    jdkHome = jdkHome,
).use { session ->
    session.kotlinFiles.findTestFiles()
}
