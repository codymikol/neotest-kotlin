package io.github.codymikol.kotlintest.command

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

public class Discover : CliktCommand() {
    private val files: List<File> by option(
        help = "Comma separated absolute paths to files for discovery"
    ).file(canBeDir = false).split(",").required()

    private val output: File by option(help = "File to write the JSON test results").file().required()

    override fun run() {
        val mapper = ObjectMapper().registerKotlinModule()
        val virtualFilesystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)

        val virtualFiles = files.mapNotNull {
            virtualFilesystem.findFileByPath(it.absolutePath)
        }

        val apiSession = buildStandaloneAnalysisAPISession {
            buildKtModuleProvider {
                platform = JvmPlatforms.defaultJvmPlatform

                val module = buildKtSourceModule {
                    moduleName = "discovery"
                    platform = JvmPlatforms.defaultJvmPlatform

                    addSourceVirtualFiles(virtualFiles)
                }

                addModule(module)
            }
        }

        val results = apiSession
            .modulesWithFiles
            .map { (_, files) -> TestDiscoverer.discoverAllTests(files as Set<KtFile>) }

        mapper.writeValue(output, results)
    }
}
