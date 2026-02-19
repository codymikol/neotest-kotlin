package io.github.codymikol.kotlintest.files.suite

import io.github.codymikol.kotlintest.files.model.TestFileType
import org.jetbrains.kotlin.psi.KtFile

internal interface IsTestContainingFile {
    val type: TestFileType
    fun isTest(kotlinFile: KtFile): Boolean
}
