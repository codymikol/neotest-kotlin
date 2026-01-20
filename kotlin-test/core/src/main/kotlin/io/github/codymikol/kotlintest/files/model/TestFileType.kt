package io.github.codymikol.kotlintest.files.model

internal enum class TestFileType {
    KotestAnnotationSpec,
    KotestBehaviorSpec,
    KotestDescribeSpec,
    KotestExpectSpec,
    KotestFeatureSpec,
    KotestFreeSpec,
    KotestFunSpec,
    KotestShouldSpec,
    KotestStringSpec,
    KotestWordSpec,
    JunitTest,
}