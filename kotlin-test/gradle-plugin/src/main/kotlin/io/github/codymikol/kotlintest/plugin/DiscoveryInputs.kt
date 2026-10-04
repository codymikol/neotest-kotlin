package io.github.codymikol.kotlintest.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaToolchainService

/*
 * Inputs of the discovery tasks resolved from the projects of the build.
 *
 * Discovery is registered on the root project and covers the test sources of the whole build,
 * so the inputs are aggregated across all projects for now.
 */

private const val DISCOVERY_CLASSPATH_USAGE = "neotest-kotlin-discovery-classpath"
internal const val DISCOVERY_CLASSPATH = "kotlinTestDiscoveryClasspath"
private const val DISCOVERY_CLASSPATH_DEPENDENCIES = "kotlinTestDiscoveryClasspathDependencies"
private const val DISCOVERY_CLASSPATH_ELEMENTS = "kotlinTestDiscoveryClasspathElements"

private fun Project.testSourceSet(): SourceSet? =
    extensions.findByType(SourceSetContainer::class.java)?.findByName(SourceSet.TEST_SOURCE_SET_NAME)

/**
 * External library artifacts (jars) of this project's `test` compile classpath.
 *
 * Project dependencies (e.g. `project(":base")`, `testFixtures(project(":base"))`) and the
 * output of `main` are intentionally excluded: their sources are part of the discovery session
 * already, and including their outputs would force compilation before discovery.
 *
 * The view is lenient so unresolvable dependencies degrade symbol resolution instead of failing discovery.
 */
private fun Project.testCompileLibraries(): FileCollection =
    testSourceSet()
        ?.let { sourceSet -> configurations.findByName(sourceSet.compileClasspathConfigurationName) }
        ?.incoming
        ?.artifactView {
            isLenient = true
            componentFilter { it is ModuleComponentIdentifier }
        }?.files
        ?: files()

/**
 * Registers, on the root project, a configuration resolving the external libraries of the `test`
 * compile classpath of every project of the build.
 *
 * Gradle doesn't allow resolving another project's configuration directly, so every Java project
 * exposes its libraries through a consumable configuration, which the root project depends on.
 */
internal fun Project.registerDiscoveryClasspath(): Provider<out Configuration> {
    val discoveryDependencies = configurations.dependencyScope(DISCOVERY_CLASSPATH_DEPENDENCIES) {
        description = "Projects whose test compile classpath is used by test discovery"
    }
    val discoveryClasspath = configurations.resolvable(DISCOVERY_CLASSPATH) {
        extendsFrom(discoveryDependencies.get())
        description = "External libraries of the test compile classpath of all projects, used by test discovery"
        attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, DISCOVERY_CLASSPATH_USAGE))
    }

    val root = this
    allprojects {
        val project = this

        pluginManager.withPlugin("java") {
            configurations.consumable(DISCOVERY_CLASSPATH_ELEMENTS) {
                description = "External libraries of the test compile classpath, used by test discovery"
                attributes.attribute(
                    Usage.USAGE_ATTRIBUTE,
                    objects.named(Usage::class.java, DISCOVERY_CLASSPATH_USAGE),
                )
                // Lazy, resolved with this project's lock when the root configuration is resolved
                outgoing.artifacts(provider { project.testCompileLibraries().files })
            }

            root.dependencies.add(
                DISCOVERY_CLASSPATH_DEPENDENCIES,
                root.dependencies.project(mapOf("path" to project.path)),
            )
        }
    }

    return discoveryClasspath
}

/**
 * The JDK home of the first project with a `test` source set, using its Java toolchain
 * (the current JVM when no toolchain is configured), falling back to the JVM running Gradle.
 *
 * This must be called once all projects are evaluated, e.g. when configuring a task.
 */
internal fun Project.discoveryJdkHome(): Provider<String> {
    val gradleJavaHome = provider { System.getProperty("java.home") }
    val project = allprojects.firstOrNull { it.testSourceSet() != null }
    val java = project?.extensions?.findByType(JavaPluginExtension::class.java)
    val toolchains = project?.extensions?.findByType(JavaToolchainService::class.java)

    return if (java != null && toolchains != null) {
        toolchains
            .launcherFor(java.toolchain)
            .map { it.metadata.installationPath.asFile.absolutePath }
            .orElse(gradleJavaHome)
    } else {
        gradleJavaHome
    }
}
