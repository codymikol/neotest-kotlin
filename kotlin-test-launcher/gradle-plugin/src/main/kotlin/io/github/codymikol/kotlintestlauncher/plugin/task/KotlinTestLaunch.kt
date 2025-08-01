package io.github.codymikol.kotlintestlauncher.plugin.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.codymikol.kotlintestlauncher.TestFrameworkRunner
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URLClassLoader

abstract class KotlinTestLaunch : DefaultTask() {
    @get:Input
    abstract val classes: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun run() {
        val java = project.extensions.getByType(JavaPluginExtension::class.java)

        val runtimeFiles =
            java
                .sourceSets
                .flatMap { it.runtimeClasspath }
                .map { it.toURI().toURL() }

        val classLoader = URLClassLoader(runtimeFiles.toTypedArray(), this.javaClass.classLoader)
        val kotlinClasses = classes.get().split(",").map { className -> classLoader.loadClass(className).kotlin }

        val report = TestFrameworkRunner.runAll(classes = kotlinClasses)
        val mapper = ObjectMapper().registerKotlinModule()

        mapper.writeValue(outputFile.asFile.get(), report)
    }
}
