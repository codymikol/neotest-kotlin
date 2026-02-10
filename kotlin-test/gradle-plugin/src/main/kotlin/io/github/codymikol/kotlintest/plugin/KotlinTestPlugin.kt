package io.github.codymikol.kotlintest.plugin

import io.github.codymikol.kotlintest.plugin.task.KotlinTestDiscoverTask
import io.github.codymikol.kotlintest.plugin.task.KotlinTestExecuteTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.StopExecutionException
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.UUID
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

@Suppress("unused") // entry point for Gradle plugin, always used.
class KotlinTestPlugin : Plugin<Project> {
    private fun Project.registerKotlinTestDiscoverTask() {
        if (project != project.rootProject) {
            return
        }

        val testKotlinFileTree = project.fileTree(project.rootDir) {
            include("**/*.kt")
            exclude("**/main/**")
            exclude("**/build/**")
            exclude("**/.gradle/**")
        }

        testKotlinFileTree.files.forEach { file ->
            val relativePath = file.toPath().relativeTo(project.rootDir.toPath())
            val taskName =
                "kotlinTestDiscover_" +
                    relativePath
                        .joinToString(separator = "_") {
                            it.fileName.pathString.substringBefore(".")
                        }

            project.tasks.register<KotlinTestDiscoverTask>(taskName) {
                group = "discovery"
                description = "Discovers tests across Kotlin frameworks for $relativePath"

                this.includeFile.set(file)
                source(testKotlinFileTree)

                this.outputFile.set(
                    project.layout.buildDirectory.file(
                        "kotlinTestDiscover/$relativePath.json"
                    )
                )
            }
        }
    }

    override fun apply(project: Project) {
        project.registerKotlinTestDiscoverTask()

        project.allprojects {
            afterEvaluate {
                // We require compileTestKotlin to load tests and run
                if (
                    project.tasks.findByName("compileTestKotlin") == null ||
                    !project.plugins.hasPlugin(JavaPlugin::class.java)
                ) {
                    return@afterEvaluate
                }

                project.tasks.register<KotlinTestExecuteTask>("kotlinTestExecute") {
                    group = "verification"
                    description = "Run tests across Kotlin frameworks"

                    // Never up to date
                    outputs.upToDateWhen { false }

                    this.dependsOn("testClasses")

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
    }
}
