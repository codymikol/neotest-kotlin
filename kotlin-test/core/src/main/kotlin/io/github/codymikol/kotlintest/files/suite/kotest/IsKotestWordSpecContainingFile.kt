package io.github.codymikol.kotlintest.files.suite.kotest

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsSubclassOfTest
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import org.jetbrains.kotlin.psi.KtFile

internal class IsKotestWordSpecContainingFile : IsTestContainingFile {
    override val type: TestFileType = TestFileType.KotestWordSpec

    private val isKotestWordSpecSubclass = IsSubclassOfTest(
        pkg = "io.kotest.core.spec.style",
        identifier = "WordSpec",
    )

    override fun isTest(kotlinFile: KtFile): Boolean = isKotestWordSpecSubclass.evaluate(kotlinFile)
}
