package io.github.codymikol.kotlintest.plugin

import io.github.codymikol.kotlintest.plugin.task.KotlinTestDiscoverTask
import io.github.codymikol.kotlintest.plugin.task.KotlinTestExecuteTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.StopExecutionException
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.UUID

@Suppress("unused") // entry point for Gradle plugin, always used.
class KotlinTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // We require compileTestKotlin to load tests and run
        if (
            project.tasks.findByName("compileTestKotlin") == null ||
            !project.plugins.hasPlugin(JavaPlugin::class.java)
        ) {
            return
        }

        project.tasks.register<KotlinTestDiscoverTask>("kotlinTestDiscover") {
            group = "verification"
            description = "Discovers tests across Kotlin frameworks"

            // Never up to date
            outputs.upToDateWhen { false }

            // Dependencies
            val sourceSets = project.extensions.findByName("sourceSets") as SourceSetContainer
            val testSourceSet =
                sourceSets.findByName("test")
                    ?: throw StopExecutionException("Could not find source set 'test'")

            val file = project.properties["file"]?.toString()

            val kotlinTestFiles = testSourceSet.allSource
                .filter { it.extension.endsWith("kt") && (file == null || it.absolutePath == file) }
                .map { it.absolutePath }
                .toSet()

            // configure Java executable
            mainClass.set(KotlinTestDiscoverTask.MAIN)
            classpath =
                project.files(
                    // include this plugin into the classpath of the executable
                    this::class.java.protectionDomain.codeSource.location,
                    project.configurations.getByName("testCompileClasspath")
                )

            this.kotlinTestFiles.setFrom(kotlinTestFiles)
            outputFile.convention(project.layout.buildDirectory.file("$name/output-${UUID.randomUUID()}.json"))
            outputFile.set(project.properties["outputFile"]?.toString()?.let { File(it) })
        }

        project.tasks.register<KotlinTestExecuteTask>("kotlinTestExecute") {
            group = "verification"
            description = "Run tests across Kotlin frameworks"

            // Never up to date
            outputs.upToDateWhen { false }

            this.dependsOn("compileTestKotlin")

            // Dependencies
            val java = project.extensions.getByType(JavaPluginExtension::class.java)
            val sourceSet =
                java.sourceSets.findByName("test")
                    ?: throw StopExecutionException("Could not find source set 'test'")

            // configure Java executable
            mainClass.set(KotlinTestExecuteTask.MAIN)
            classpath =
                project.files(
                    // include this plugin into the classpath of the executable
                    this::class.java.protectionDomain.codeSource.location,
                    sourceSet.runtimeClasspath,
                )

            testSourceSetClasspath.set(sourceSet.runtimeClasspath)
            classes.set(project.properties["classes"]?.toString())
            outputFile.convention(project.layout.buildDirectory.file("$name/output-${UUID.randomUUID()}.json"))
            outputFile.set(project.properties["outputFile"]?.toString()?.let { File(it) })
            filter.set(project.properties["filter"]?.toString())
        }
    }
}
