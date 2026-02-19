package io.github.codymikol.kotlintest.files.suite.kotest

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsSubclassOfTest
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import org.jetbrains.kotlin.psi.KtFile

internal class IsKotestAnnotationSpecContainingFile : IsTestContainingFile {

    override val type: TestFileType = TestFileType.KotestAnnotationSpec

    private val isKotestAnnotationSpecSubclass = IsSubclassOfTest(
        pkg = "io.kotest.core.spec.style",
        identifier = "AnnotationSpec",
    )

    override fun isTest(kotlinFile: KtFile): Boolean = isKotestAnnotationSpecSubclass.evaluate(kotlinFile)
}
