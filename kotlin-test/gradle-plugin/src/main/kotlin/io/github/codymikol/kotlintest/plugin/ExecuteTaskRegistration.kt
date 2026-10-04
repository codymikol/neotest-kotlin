package io.github.codymikol.kotlintest.plugin

import io.github.codymikol.kotlintest.plugin.task.KotlinTestExecuteTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.UUID

internal const val KOTLIN_TEST_EXECUTE_TASK = "kotlinTestExecute"

/**
 * Configuration holding `junit-platform-launcher` when the project doesn't already have it on its
 * test runtime classpath.
 */
internal const val JUNIT_PLATFORM_LAUNCHER_CONFIGURATION = "kotlinTestJUnitPlatformLauncher"

/**
 * Registers `kotlinTestExecute`, which runs tests in a separate JVM with the test runtime classpath of the
 * project followed by the `runner` jar. Tests therefore run with the project's own Kotest, JUnit Platform
 * and kotlin-stdlib versions, and nothing used for discovery (such as the Kotlin compiler) leaks in.
 */
internal fun Project.registerKotlinTestExecuteTask() {
    // We require compileTestKotlin to load tests and run. The plugin may be applied both to the root
    // project and to each subproject (e.g. by an init script), so only register once.
    if (
        KOTLIN_TEST_EXECUTE_TASK in tasks.names ||
        tasks.findByName("compileTestKotlin") == null ||
        !plugins.hasPlugin(JavaPlugin::class.java)
    ) {
        return
    }

    val sourceSet = extensions.getByType(JavaPluginExtension::class.java).sourceSets.findByName("test") ?: return

    val testRuntimeModules =
        configurations
            .getByName(sourceSet.runtimeClasspathConfigurationName)
            .incoming
            .resolutionResult
            .rootComponent
            .map { it.moduleVersions() }

    val junitPlatformLauncher =
        configurations.create(JUNIT_PLATFORM_LAUNCHER_CONFIGURATION) {
            description = "junit-platform-launcher aligned with the project's JUnit Platform version, if missing"
            isCanBeConsumed = false
            isCanBeResolved = true
            isTransitive = false
        }
    junitPlatformLauncher.dependencies.addAllLater(
        testRuntimeModules.map { modules ->
            listOfNotNull(
                ExecutionCompatibility.junitPlatformLauncherFor(modules)?.let { dependencies.create(it) },
            )
        },
    )

    tasks.register<KotlinTestExecuteTask>(KOTLIN_TEST_EXECUTE_TASK) {
        group = "verification"
        description = "Run tests across Kotlin frameworks"

        // Never up to date
        outputs.upToDateWhen { false }

        this.dependsOn("testClasses")

        // configure Java executable, the runner jar is appended when executing
        mainClass.set(KotlinTestExecuteTask.MAIN)
        classpath = files(sourceSet.runtimeClasspath, junitPlatformLauncher)

        // Compiled test output (and the tasks producing it) as inputs, without
        // referencing Kotlin Gradle Plugin types that aren't visible to init script classloaders.
        inputs.files(sourceSet.output)
        testSourceSetClasspath.set(sourceSet.runtimeClasspath)
        this.testRuntimeModules.set(testRuntimeModules)
        classes.set(project.properties["classes"]?.toString())
        outputFile.convention(layout.buildDirectory.file("$name/output-${UUID.randomUUID()}.json"))
        outputFile.set(project.properties["outputFile"]?.toString()?.let { File(it) })
        filter.set(project.properties["filter"]?.toString())
    }
}
