package io.github.codymikol.kotlintest.files.suite.kotest

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import io.github.codymikol.kotlintest.files.suite.IsSubclassOfTest
import org.jetbrains.kotlin.psi.KtFile

internal class IsKotestExpectSpecContainingFile : IsTestContainingFile {

    override val type: TestFileType = TestFileType.KotestExpectSpec

    private val isKotestExpectSpecSubclass = IsSubclassOfTest(
        pkg = "io.kotest.core.spec.style",
        identifier = "ExpectSpec",
    )

    override fun isTest(kotlinFile: KtFile): Boolean = isKotestExpectSpecSubclass.evaluate(kotlinFile)


}