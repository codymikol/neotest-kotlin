package io.github.codymikol.kotlintest.extensions

import org.jetbrains.kotlin.analysis.api.standalone.StandaloneAnalysisAPISession
import org.jetbrains.kotlin.psi.KtFile

internal fun StandaloneAnalysisAPISession.getKtFiles(): List<KtFile> = modulesWithFiles
    .values
    .flatten()
    .filterIsInstance<KtFile>()
