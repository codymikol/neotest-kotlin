package io.github.codymikol.kotlintest.files.suite.kotest

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import io.github.codymikol.kotlintest.files.suite.IsSubclassOfTest
import org.jetbrains.kotlin.psi.KtFile

internal class IsKotestFunSpecContainingFile : IsTestContainingFile {

    override val type: TestFileType = TestFileType.KotestFunSpec

    private val isKotestFunSpecSubclass = IsSubclassOfTest(
        pkg = "io.kotest.core.spec.style",
        identifier = "FunSpec",
    )

    override fun isTest(kotlinFile: KtFile): Boolean = isKotestFunSpecSubclass.evaluate(kotlinFile)

}