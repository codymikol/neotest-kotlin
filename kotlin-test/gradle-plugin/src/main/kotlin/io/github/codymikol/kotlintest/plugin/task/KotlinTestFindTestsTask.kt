package io.github.codymikol.kotlintest.plugin.task

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile

abstract class KotlinTestFindTestsTask : JavaExec() {

    companion object {
        const val MAIN = "io.github.codymikol.kotlintest.MainKt"
    }

    @get:InputFiles
    abstract val allFilesInTestSource: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    override fun exec() {

        val outputFile = this@KotlinTestFindTestsTask.outputFile.asFile.get()

        println("Executing: $MAIN files --output=$outputFile")

        this.args(
            listOf(
                "files",
                "--files=${allFilesInTestSource.joinToString(",") { it.absolutePath }}",
                "--output=$outputFile",
            ),
        )

        super.exec()
    }
}