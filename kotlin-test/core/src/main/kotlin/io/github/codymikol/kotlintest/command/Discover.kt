package io.github.codymikol.kotlintest.command

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.core.CliktCommand
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

    private val output: File by option(help = "File to write the JSON test results").file().required()

    override fun run() {
        val mapper = ObjectMapper().registerKotlinModule()

        val session = AnalysisApiSession(files.map { Analysis.File(it) })
        val filesToDiscover = session.kotlinFiles
            .filter { kotlinFile ->
                includeFiles == null || includeFiles
                    ?.map { it.absolutePath }
                    ?.contains(kotlinFile.virtualFilePath) == true
            }.toSet()

        val result = TestDiscoverer.discoverAllTests(filesToDiscover)

        mapper.writeValue(output, result)
        exitProcess(0) // hangs forever otherwise, but we're done?
    }
}

/**
 * Performs Kotlin Analysis discovery using [files] as all dependencies
 * and [include] as the [File] to perform discovery on specifically. If [include]
 * is null discovery will be performed on all tests.
 */
public fun discover(files: Collection<File>, include: File? = null): DiscoveredResult {
    val session = AnalysisApiSession(files.map { Analysis.File(it) })
    val filesToDiscover = session.kotlinFiles
        .filter { kotlinFile ->
            include == null || include.absolutePath == kotlinFile.virtualFilePath
        }.toSet()

    return TestDiscoverer.discoverAllTests(filesToDiscover)
}
