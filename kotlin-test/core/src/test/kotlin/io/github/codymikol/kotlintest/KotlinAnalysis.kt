package io.github.codymikol.kotlintest

import io.github.codymikol.kotlintest.provider.Analysis
import io.github.codymikol.kotlintest.provider.AnalysisApiSession
import org.jetbrains.kotlin.psi.KtFile

internal fun createKtFile(filename: String, code: String): KtFile {
    val session = AnalysisApiSession(
        files = listOf(Analysis.VirtualFile(filename, code)),
        unitTestMode = true
    )

    return session.kotlinFiles.first { it.name == filename }
}
