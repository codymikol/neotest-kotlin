package io.github.codymikol.kotlintest.files.model

internal data class IsTestFileResult(
    val path: String,
    val types: List<TestFileType>
) {

    fun isTestFile(): Boolean = types.isNotEmpty()
}
