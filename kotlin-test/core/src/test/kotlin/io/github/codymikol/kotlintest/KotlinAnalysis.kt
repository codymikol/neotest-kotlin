package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.provider.Analysis
import io.github.codymikol.kotlintest.provider.AnalysisApiSession
import org.jetbrains.kotlin.psi.KtFile

internal fun createKtFile(filename: String, code: String, dependencies: Collection<Analysis> = emptyList()): KtFile {
    val session = AnalysisApiSession(
        files = listOf(Analysis.VirtualFile(filename, code)) + dependencies,
        unitTestMode = true
    )

    return session.kotlinFiles.first { it.name == filename }
}
