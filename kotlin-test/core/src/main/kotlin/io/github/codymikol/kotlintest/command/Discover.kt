package io.github.codymikol.kotlintest.command

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.provider.kotestStubImplVirtualFile
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import kotlin.system.exitProcess

public class Discover : CliktCommand() {
    private val files: List<File> by option(
        help = "Comma separated absolute paths to files for discovery"
    ).file(canBeDir = false).split(",").required()

    private val output: File by option(help = "File to write the JSON test results").file().required()

    override fun run() {
        val mapper = ObjectMapper().registerKotlinModule()

        val apiSession = buildStandaloneAnalysisAPISession {
            val targetPlatform = JvmPlatforms.defaultJvmPlatform

            buildKtModuleProvider {
                platform = targetPlatform

                addModule(
                    buildKtSourceModule {
                        platform = targetPlatform
                        moduleName = "source"
                        addSourceRoots(files.map { it.toPath() })
                        addSourceVirtualFile(kotestStubImplVirtualFile())
                    }
                )
            }
        }

        val result = apiSession
            .modulesWithFiles
            .map { (_, files) -> TestDiscoverer.discoverAllTests(files.toSet() as Set<KtFile>) }
            .fold(DiscoveredResult()) { acc, result ->
                acc.copy(
                    tests = acc.tests + result.tests,
                    warnings = acc.warnings + result.warnings
                )
            }

        mapper.writeValue(output, result)
        exitProcess(0) // hangs forever otherwise, but we're done?
    }
}
