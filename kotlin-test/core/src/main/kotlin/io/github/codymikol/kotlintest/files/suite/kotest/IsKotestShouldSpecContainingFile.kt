package io.github.codymikol.kotlintest.files.suite.kotest

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsSubclassOfTest
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import org.jetbrains.kotlin.psi.KtFile

internal class IsKotestShouldSpecContainingFile : IsTestContainingFile {
    override val type: TestFileType = TestFileType.KotestShouldSpec

    private val isKotestShouldSpecSubclass = IsSubclassOfTest(
        pkg = "io.kotest.core.spec.style",
        identifier = "ShouldSpec",
    )

    override fun isTest(kotlinFile: KtFile): Boolean = isKotestShouldSpecSubclass.evaluate(kotlinFile)
}
