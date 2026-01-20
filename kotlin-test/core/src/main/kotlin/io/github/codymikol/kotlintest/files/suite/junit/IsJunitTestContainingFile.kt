package io.github.codymikol.kotlintest.files.suite.junit

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import org.jetbrains.kotlin.psi.KtFile

internal class IsJunitTestContainingFile : IsTestContainingFile {
    override val type: TestFileType = TestFileType.JunitTest

    override fun isTest(kotlinFile: KtFile): Boolean {
        return false
    }
}