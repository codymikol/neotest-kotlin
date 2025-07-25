package io.github.codymikol.kotlintestlauncher.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinTestLauncherPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("test") {
            group = "verification"
            description = "Run tests across Kotlin frameworks"

            doLast {
                println("Discovering and running tests... (demo)")
                // Hook into core discovery here
            }
        }
    }
}
