package io.github.codymikol.kotlintest.files

import io.github.codymikol.kotlintest.files.model.IsTestFileResult
import io.github.codymikol.kotlintest.files.suite.junit.IsJunitTestContainingFile
import io.github.codymikol.kotlintest.files.suite.kotest.IsKotestAnnotationSpecContainingFile
import io.github.codymikol.kotlintest.files.suite.kotest.IsKotestBehaviorSpecContainingFile
import io.github.codymikol.kotlintest.files.suite.kotest.IsKotestDescribeSpecContainingFile
import io.github.codymikol.kotlintest.files.suite.kotest.IsKotestExpectSpecContainingFile
import io.github.codymikol.kotlintest.files.suite.kotest.IsKotestFeatureSpecContainingFile
import io.github.codymikol.kotlintest.files.suite.kotest.IsKotestFreeSpecContainingFile
import io.github.codymikol.kotlintest.files.suite.kotest.IsKotestFunSpecContainingFile
import io.github.codymikol.kotlintest.files.suite.kotest.IsKotestShouldSpecContainingFile
import io.github.codymikol.kotlintest.files.suite.kotest.IsKotestStringSpecContainingFile
import io.github.codymikol.kotlintest.files.suite.kotest.IsKotestWordSpecContainingFile
import org.jetbrains.kotlin.psi.KtFile

internal class IsTestContainingFileExecutor {

    private val testTesters = listOf(
        IsKotestAnnotationSpecContainingFile(),
        IsKotestBehaviorSpecContainingFile(),
        IsKotestDescribeSpecContainingFile(),
        IsKotestExpectSpecContainingFile(),
        IsKotestFeatureSpecContainingFile(),
        IsKotestFreeSpecContainingFile(),
        IsKotestFunSpecContainingFile(),
        IsKotestShouldSpecContainingFile(),
        IsJunitTestContainingFile(),
        IsKotestStringSpecContainingFile(),
        IsKotestWordSpecContainingFile(),
    )

    fun getTestFileResult(file: KtFile): IsTestFileResult {
        val positiveResults = testTesters.filter { it.isTest(file) }

        val typesOfTestsInFile = positiveResults.map { it.type }

        return IsTestFileResult(types = typesOfTestsInFile, path = file.virtualFilePath)
    }
}
