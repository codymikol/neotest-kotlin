package io.github.codymikol.kotlintest.plugin.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintest.command.discover
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class KotlinTestDiscoverTask : SourceTask() {
    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val includeFile: RegularFileProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val results =
            discover(
                files = source.files,
                include = includeFile.get().asFile,
            )

        val objectMapper = ObjectMapper().registerKotlinModule()
        objectMapper.writeValue(outputFile.get().asFile, results)
    }
}
