package io.github.codymikol.kotlintest.plugin

import io.github.codymikol.kotlintest.plugin.task.KotlinTestDiscoverTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
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

        // Production sources, used only to resolve symbols referenced by tests
        val mainSourceFileTree = project.fileTree(project.rootDir) {
            include("**/main/**/*.kt")
            include("**/main/**/*.java")
            exclude("**/build/**")
            exclude("**/.gradle/**")
        }

        val discoveryClasspath = project.registerDiscoveryClasspath()

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
                this.mainSources.from(mainSourceFileTree)
                this.testCompileClasspath.from(discoveryClasspath)
                // Configured lazily when the task is realized, after all projects are evaluated
                this.jdkHome.set(project.discoveryJdkHome())

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
        project.registerKotlinTestFindTestsTask()

        project.allprojects {
            afterEvaluate {
                registerKotlinTestExecuteTask()
            }
        }
    }
}
