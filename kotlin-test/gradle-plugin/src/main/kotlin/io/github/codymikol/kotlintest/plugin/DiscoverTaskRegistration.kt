package io.github.codymikol.kotlintest.plugin

import io.github.codymikol.kotlintest.plugin.task.KotlinTestDiscoverTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.register
import java.io.File

internal const val KOTLIN_TEST_DISCOVER_TASK = "kotlinTestDiscover"

/**
 * System property (`-D`) with the path of the file to discover tests in, absolute or relative to the root project.
 */
internal const val DISCOVER_FILE_PROPERTY = "kotlinTestDiscoverFile"

/**
 * System property (`-D`) with the path of the JSON file to write the discovered tests to, keyed by the file
 * (e.g. `build/kotlinTestDiscover/<path of the file>.json`) so the outputs of different files don't replace each
 * other. Defaults to `build/kotlinTestDiscover/discovered.json` of the project.
 */
internal const val DISCOVER_OUTPUT_PROPERTY = "kotlinTestDiscoverOutput"

/**
 * Registers `kotlinTestDiscover`, which discovers the tests of a single file of this project's tests:
 *
 * ```
 * ./gradlew :app:kotlinTestDiscover \
 *   -DkotlinTestDiscoverFile=/path/to/Spec.kt \
 *   -DkotlinTestDiscoverOutput=/path/to/Spec.json
 * ```
 *
 * A single task per project takes the file as a system property, so the configuration doesn't depend on the files
 * of the project: adding a file doesn't invalidate the configuration cache, and neither does discovering another
 * file (system properties are only read when the task executes, unlike Gradle properties which are all part of
 * the configuration cache key). The output is keyed by the file, so the task stays cacheable.
 *
 * The Analysis API session is built from this project only: its test sources (every source set but `main` and
 * `testFixtures`), its `main` sources, the sources of the projects it depends on and the external libraries of its
 * `test` compile classpath, see `DiscoveryInputs.kt`.
 */
internal fun Project.registerKotlinTestDiscoverTask() {
    if (!plugins.hasPlugin(JavaPlugin::class.java)) {
        return
    }

    // Other projects may depend on this one even if it has no tests
    registerDiscoverySourcesElements()

    // The plugin may be applied both to the root project and to each subproject (e.g. by an init script)
    val test = sourceSet(SourceSet.TEST_SOURCE_SET_NAME)
    if (test == null || KOTLIN_TEST_DISCOVER_TASK in tasks.names) {
        return
    }
    val main = sourceSet(SourceSet.MAIN_SOURCE_SET_NAME)

    val rootDirectory = rootDir
    val includeFile =
        providers
            .systemProperty(DISCOVER_FILE_PROPERTY)
            .map { path -> File(path).let { if (it.isAbsolute) it else rootDirectory.resolve(path) }.normalize() }
    val dependencySources = dependencySources(test)
    val testCompileLibraries = testCompileLibraries(test)

    tasks.register<KotlinTestDiscoverTask>(KOTLIN_TEST_DISCOVER_TASK) {
        group = "discovery"
        description = "Discovers tests across Kotlin frameworks in the file of the '$DISCOVER_FILE_PROPERTY' property"

        this.includeFile.fileProvider(includeFile)
        // like `kotlinTestFindTests`, every source set that may contain tests
        source(testSourceSets().map { sourceSets -> sourceSets.map { sourceFiles(it) } })
        main?.let { this.mainSources.from(sourceFiles(it)) }
        this.dependencySources.from(dependencySources)
        this.testCompileClasspath.from(testCompileLibraries)
        this.jdkHome.set(discoveryJdkHome())

        outputFile.fileProvider(providers.systemProperty(DISCOVER_OUTPUT_PROPERTY).map(::File))
        outputFile.convention(layout.buildDirectory.file("kotlinTestDiscover/discovered.json"))
    }
}
