package io.github.codymikol.kotlintest.command

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.Disposable
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import com.intellij.openapi.util.Disposer

public class Discover : CliktCommand() {
    private val files: List<File> by option(
        help = "Comma separated absolute paths to files for discovery"
    ).file(canBeDir = false).split(",").required()

    private val output: File by option(help = "File to write the JSON test results").file().required()
    private val disposable: Disposable = Disposer.newDisposable()

    override fun run() {
        val mapper = ObjectMapper().registerKotlinModule()

        val apiSession = buildStandaloneAnalysisAPISession(disposable) {
            val targetPlatform = JvmPlatforms.defaultJvmPlatform

            buildKtModuleProvider {
                platform = targetPlatform

                addModule(
                    buildKtSourceModule {
                        platform = targetPlatform
                        moduleName = "source"
                        addSourceRoots(files.map { it.toPath() })
                    }
                )
            }
        }

        val results = apiSession
            .modulesWithFiles
            .map { (_, files) -> TestDiscoverer.discoverAllTests(files.toSet() as Set<KtFile>) }

        mapper.writeValue(output, results)
    }
}
