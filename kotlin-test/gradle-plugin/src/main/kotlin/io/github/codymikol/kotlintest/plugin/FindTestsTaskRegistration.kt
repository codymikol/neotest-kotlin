package io.github.codymikol.kotlintest.plugin

import io.github.codymikol.kotlintest.plugin.task.KotlinTestFindTestsTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.register

internal const val FIND_TESTS_TASK_NAME = "kotlinTestFindTests"

/**
 * Output directory of [FIND_TESTS_TASK_NAME], relative to the root project directory. The task of every project
 * writes the test files of that project to a file named by [projectFileName] in it.
 */
internal const val FIND_TESTS_OUTPUT_DIRECTORY = "build/kotlinTestFindTests"

/**
 * Registers [FIND_TESTS_TASK_NAME] on this project, determining in a single run which Kotlin files of the
 * project contain tests. Running `kotlinTestFindTests` from the root project runs it in every project.
 *
 * It uses the same inputs as discovery of this project (see `DiscoveryInputs.kt`): the files of every source set
 * that may contain tests (all but `main` and `testFixtures`), with the `main` sources, the sources of the
 * projects it depends on, the external libraries of the `test` compile classpath and the JDK used to resolve
 * symbols.
 */
internal fun Project.registerKotlinTestFindTestsTask() {
    // The plugin may be applied both to the root project and to each subproject (e.g. by an init script)
    val test = sourceSet(SourceSet.TEST_SOURCE_SET_NAME)
    if (!plugins.hasPlugin(JavaPlugin::class.java) || test == null || FIND_TESTS_TASK_NAME in tasks.names) {
        return
    }
    val main = sourceSet(SourceSet.MAIN_SOURCE_SET_NAME)

    val dependencySources = dependencySources(test)
    val testCompileLibraries = testCompileLibraries(test)
    val outputFile = rootDir.resolve(FIND_TESTS_OUTPUT_DIRECTORY).resolve(projectFileName(path))

    tasks.register<KotlinTestFindTestsTask>(FIND_TESTS_TASK_NAME) {
        group = "discovery"
        description = "Determines which Kotlin files of the project contain tests"

        source(testSourceSets().map { sourceSets -> sourceSets.map { sourceFiles(it) } })
        main?.let { mainSources.from(sourceFiles(it)) }
        this.dependencySources.from(dependencySources)
        testCompileClasspath.from(testCompileLibraries)
        jdkHome.set(discoveryJdkHome())

        this.outputFile.set(outputFile)
    }
}
