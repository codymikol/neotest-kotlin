package io.github.codymikol.kotlintest.plugin

import io.github.codymikol.kotlintest.plugin.task.KotlinTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.UUID

@Suppress("unused") // entry point for Gradle plugin, always used.
class KotlinTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // We require compileTestKotlin to load tests and run
        if (project.tasks.findByName("compileTestKotlin") == null) {
            return
        }

        project.tasks.register<KotlinTestTask>("kotlinTest") {
            group = "verification"
            description = "Run tests across Kotlin frameworks"

            // Never up to date
            outputs.upToDateWhen { false }

            this.dependsOn("compileTestKotlin")

            // Dependencies
            classes.set(project.properties["classes"]?.toString())
            outputFile.convention(project.layout.buildDirectory.file("$name/output-${UUID.randomUUID()}.json"))
            outputFile.set(project.properties["outputFile"]?.toString()?.let { File(it) })
        }
    }
}
