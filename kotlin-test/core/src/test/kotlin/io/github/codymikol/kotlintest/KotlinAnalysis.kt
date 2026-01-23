package io.github.codymikol.kotlintest

import com.intellij.testFramework.LightVirtualFile
import io.github.codymikol.kotlintest.provider.kotestStubImplVirtualFile
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile

internal fun createKtFile(filename: String, code: String): KtFile {
    val session = buildStandaloneAnalysisAPISession(unitTestMode = true) {
        buildKtModuleProvider {
            platform = JvmPlatforms.defaultJvmPlatform

            val module = buildKtSourceModule {
                moduleName = "test"
                platform = JvmPlatforms.defaultJvmPlatform
                addSourceVirtualFile(LightVirtualFile(filename, KotlinLanguage.INSTANCE, code))
                addSourceVirtualFile(kotestStubImplVirtualFile())
            }

            addModule(module)
        }
    }

    return session.modulesWithFiles
        .flatMap { it.value }
        .first { it.name == filename } as KtFile
}
