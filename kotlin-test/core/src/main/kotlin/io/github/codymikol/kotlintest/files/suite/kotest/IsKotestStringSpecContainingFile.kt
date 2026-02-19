package io.github.codymikol.kotlintest.files.suite.kotest

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsSubclassOfTest
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import org.jetbrains.kotlin.psi.KtFile

internal class IsKotestStringSpecContainingFile : IsTestContainingFile {

    override val type: TestFileType = TestFileType.KotestStringSpec

    private val isKotestStringSpecSubclass = IsSubclassOfTest(
        pkg = "io.kotest.core.spec.style",
        identifier = "StringSpec",
    )

    override fun isTest(kotlinFile: KtFile): Boolean = isKotestStringSpecSubclass.evaluate(kotlinFile)
}
