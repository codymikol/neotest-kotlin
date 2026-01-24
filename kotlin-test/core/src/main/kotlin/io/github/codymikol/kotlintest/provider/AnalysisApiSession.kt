package io.github.codymikol.kotlintest.provider

import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile

internal sealed interface Analysis {
    @JvmInline
    value class File(val value: java.io.File) : Analysis

    data class VirtualFile(val filename: String, val code: String) : Analysis
}

internal class AnalysisApiSession(
    private val files: List<Analysis>,
    private val unitTestMode: Boolean = false
) {
    internal val kotlinFiles: Set<KtFile>
        get(): Set<KtFile> {
            val apiSession = buildStandaloneAnalysisAPISession(unitTestMode = unitTestMode) {
                val targetPlatform = JvmPlatforms.defaultJvmPlatform

                buildKtModuleProvider {
                    platform = targetPlatform

                    addModule(
                        buildKtSourceModule {
                            platform = targetPlatform
                            moduleName = "source"

                            addSourceVirtualFile(kotestStubImplVirtualFile())

                            files.forEach { file ->
                                when (file) {
                                    is Analysis.File -> addSourceRoot(file.value.toPath())
                                    is Analysis.VirtualFile -> addSourceVirtualFile(
                                        LightVirtualFile(
                                            file.filename,
                                            KotlinLanguage.INSTANCE,
                                            file.code
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            }

            return apiSession
                .modulesWithFiles
                .values
                .flatten()
                // Filter out KoTest stubs that aren't real tests
                .filter { it.name != kotestStubImplVirtualFile().name }
                .filterIsInstance<KtFile>()
                .toSet()
        }
}
