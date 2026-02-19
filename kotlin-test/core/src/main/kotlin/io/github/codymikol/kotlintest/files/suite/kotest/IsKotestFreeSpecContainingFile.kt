package io.github.codymikol.kotlintest.files.suite.kotest

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsSubclassOfTest
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import org.jetbrains.kotlin.psi.KtFile

internal class IsKotestFreeSpecContainingFile : IsTestContainingFile {
    override val type: TestFileType = TestFileType.KotestFreeSpec

    private val isKotestFreeSpecSubclass = IsSubclassOfTest(
        pkg = "io.kotest.core.spec.style",
        identifier = "FreeSpec",
    )

    override fun isTest(kotlinFile: KtFile): Boolean = isKotestFreeSpecSubclass.evaluate(kotlinFile)
}
