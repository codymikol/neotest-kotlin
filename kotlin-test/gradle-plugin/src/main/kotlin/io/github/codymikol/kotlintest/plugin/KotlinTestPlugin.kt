package io.github.codymikol.kotlintest.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused") // entry point for Gradle plugin, always used.
class KotlinTestPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.allprojects {
            afterEvaluate {
                registerKotlinTestDiscoverTask()
                registerKotlinTestFindTestsTask()
                registerKotlinTestExecuteTask()
            }
        }
    }
}
