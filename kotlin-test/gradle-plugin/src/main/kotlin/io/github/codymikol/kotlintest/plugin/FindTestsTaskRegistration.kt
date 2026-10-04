package io.github.codymikol.kotlintest.plugin

import io.github.codymikol.kotlintest.plugin.task.KotlinTestFindTestsTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

internal const val FIND_TESTS_TASK_NAME = "kotlinTestFindTests"

/**
 * Output of [FIND_TESTS_TASK_NAME], relative to the build directory of the root project.
 */
internal const val FIND_TESTS_OUTPUT = "kotlinTestFindTests/test-files.json"

/**
 * Registers [FIND_TESTS_TASK_NAME] on the root project, determining in a single run which Kotlin
 * files of the whole build contain tests.
 *
 * It uses the same inputs as discovery: every `.kt` file outside of `main` sources, with the
 * `main` sources, the test compile classpath and the JDK used to resolve symbols.
 *
 * Must be called after discovery registered the discovery classpath on the root project.
 */
internal fun Project.registerKotlinTestFindTestsTask() {
    if (this != rootProject) {
        return
    }

    val testKotlinFileTree = fileTree(rootDir) {
        include("**/*.kt")
        exclude("**/main/**")
        exclude("**/build/**")
        exclude("**/.gradle/**")
    }

    val mainSourceFileTree = fileTree(rootDir) {
        include("**/main/**/*.kt")
        include("**/main/**/*.java")
        exclude("**/build/**")
        exclude("**/.gradle/**")
    }

    val discoveryClasspath = configurations.named(DISCOVERY_CLASSPATH)

    tasks.register<KotlinTestFindTestsTask>(FIND_TESTS_TASK_NAME) {
        group = "discovery"
        description = "Determines which Kotlin files of the build contain tests"

        source(testKotlinFileTree)
        mainSources.from(mainSourceFileTree)
        testCompileClasspath.from(discoveryClasspath)
        // Configured lazily when the task is realized, after all projects are evaluated
        jdkHome.set(discoveryJdkHome())

        outputFile.set(layout.buildDirectory.file(FIND_TESTS_OUTPUT))
    }
}
