package io.github.codymikol.kotlintestlauncher.plugin

import io.github.codymikol.kotlintestlauncher.plugin.task.KotlinTestLaunch
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.UUID

class KotlinTestLauncherPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register<KotlinTestLaunch>("kotlinTestLaunch") {
            // Depends on Kotlin compilation to use classes
            dependsOn("compileTestKotlin")

            group = "verification"
            description = "Run tests across Kotlin frameworks"

            classes.set(project.properties["classes"]?.toString())

            outputFile.convention(project.layout.buildDirectory.file("$name/output-${UUID.randomUUID()}.json"))
            outputFile.set(project.properties["outputFile"]?.toString()?.let { File(it) })
        }
    }
}
