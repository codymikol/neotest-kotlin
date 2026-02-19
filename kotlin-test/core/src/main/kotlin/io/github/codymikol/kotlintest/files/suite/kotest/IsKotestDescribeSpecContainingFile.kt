package io.github.codymikol.kotlintest.files.suite.kotest

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsSubclassOfTest
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import org.jetbrains.kotlin.psi.KtFile

internal class IsKotestDescribeSpecContainingFile : IsTestContainingFile {

    override val type: TestFileType = TestFileType.KotestDescribeSpec

    private val isKotestDescribeSpecSubclass = IsSubclassOfTest(
        pkg = "io.kotest.core.spec.style",
        identifier = "DescribeSpec",
    )

    override fun isTest(kotlinFile: KtFile): Boolean = isKotestDescribeSpecSubclass.evaluate(kotlinFile)
}
