package io.github.codymikol.kotlintest.files.suite.kotest

import io.github.codymikol.kotlintest.files.model.TestFileType
import io.github.codymikol.kotlintest.files.suite.IsSubclassOfTest
import io.github.codymikol.kotlintest.files.suite.IsTestContainingFile
import org.jetbrains.kotlin.psi.KtFile

internal class IsKotestBehaviorSpecContainingFile : IsTestContainingFile {

    override val type: TestFileType = TestFileType.KotestBehaviorSpec

    private val isKotestBehaviorSpecSubclass = IsSubclassOfTest(
        pkg = "io.kotest.core.spec.style",
        identifier = "BehaviorSpec",
    )

    override fun isTest(kotlinFile: KtFile): Boolean = isKotestBehaviorSpecSubclass.evaluate(kotlinFile)
}
