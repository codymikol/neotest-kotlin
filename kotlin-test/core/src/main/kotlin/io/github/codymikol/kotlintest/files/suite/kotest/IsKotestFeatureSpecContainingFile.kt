package io.github.codymikol.kotlintest.files.suite.kotest

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import io.github.codymikol.kotlintest.files.suite.IsSubclassOfTest
import org.jetbrains.kotlin.psi.KtFile

internal class IsKotestFeatureSpecContainingFile : IsTestContainingFile {

    override val type: TestFileType = TestFileType.KotestFeatureSpec

    private val isKotestFeatureSpecSubclass = IsSubclassOfTest(
        pkg = "io.kotest.core.spec.style",
        identifier = "FeatureSpec",
    )

    override fun isTest(kotlinFile: KtFile): Boolean = isKotestFeatureSpecSubclass.evaluate(kotlinFile)

}