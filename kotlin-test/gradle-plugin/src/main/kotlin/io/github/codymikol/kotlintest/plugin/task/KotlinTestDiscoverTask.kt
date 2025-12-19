package io.github.codymikol.kotlintest.plugin.task

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.OutputFile

abstract class KotlinTestDiscoverTask : JavaExec() {
    companion object {
        const val MAIN = "io.github.codymikol.kotlintest.MainKt"
    }

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:InputFiles
    abstract val kotlinTestFiles: ConfigurableFileCollection

    override fun exec() {
        val files = kotlinTestFiles.joinToString(separator = ",") { it.absolutePath }
        val outputFile = this@KotlinTestDiscoverTask.outputFile.asFile.get()

        println("Executing: $MAIN discover --files=$files --output=$outputFile")

        this.args(
            listOfNotNull(
                "discover",
                "--files=$files",
                "--output=$outputFile",
            ),
        )

        super.exec()
    }
}
