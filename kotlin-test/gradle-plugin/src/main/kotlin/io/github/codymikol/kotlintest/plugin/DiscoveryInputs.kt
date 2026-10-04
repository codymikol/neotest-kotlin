package io.github.codymikol.kotlintest.plugin

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaToolchainService
import java.io.File

/*
 * Inputs of the discovery task of a project, resolved for that project only:
 * - its `test` sources, where tests are discovered
 * - its `main` sources
 * - the sources of the projects its tests depend on (`project(":other")`, `testFixtures(project(":other"))`, ...),
 *   resolved through the consumable configurations every project exposes, see [registerDiscoverySourcesElements]
 * - the external libraries of its `test` compile classpath
 * - its Java toolchain
 *
 * Only plain Gradle APIs are used: classes of the Kotlin Gradle Plugin aren't visible to this plugin
 * when it is applied by an init script.
 */

private const val DISCOVERY_SOURCES_USAGE = "neotest-kotlin-discovery-sources"
private const val DISCOVERY_SOURCES = "kotlinTestDiscoverySources"
private const val DISCOVERY_SOURCES_DEPENDENCIES = "kotlinTestDiscoverySourcesDependencies"
private const val DISCOVERY_SOURCES_ELEMENTS = "kotlinTestDiscoverySourcesElements"
private const val DEPENDENCIES_SUFFIX = "Dependencies"
private const val TEST_FIXTURES_SOURCE_SET = "testFixtures"

private val SOURCE_PATTERNS = listOf("**/*.kt", "**/*.java")

internal fun Project.sourceSet(name: String): SourceSet? =
    extensions.findByType(SourceSetContainer::class.java)?.findByName(name)

/**
 * Source sets that may contain tests: every source set but `main` and `testFixtures` (e.g. `test`,
 * `integrationTest`), evaluated lazily.
 */
internal fun Project.testSourceSets(): Provider<List<SourceSet>> =
    provider {
        extensions
            .findByType(SourceSetContainer::class.java)
            ?.filter { it.name != SourceSet.MAIN_SOURCE_SET_NAME && it.name != TEST_FIXTURES_SOURCE_SET }
            .orEmpty()
    }

/**
 * Kotlin and Java source files of the [sourceSet], evaluated lazily.
 */
internal fun Project.sourceFiles(sourceSet: SourceSet): FileTree =
    files(sourceDirectories(sourceSet)).asFileTree.matching { include(SOURCE_PATTERNS) }

/**
 * Source directories of the [sourceSet]. Kotlin directories come from the `kotlin` source directory set the
 * Kotlin Gradle Plugin adds to every source set (looked up by name, as its types aren't visible here), along
 * with the conventional `src/<name>/kotlin` directory.
 */
private fun Project.sourceDirectories(sourceSet: SourceSet): Provider<Set<File>> =
    provider {
        buildSet {
            addAll(sourceSet.java.srcDirs)
            (sourceSet.extensions.findByName("kotlin") as? SourceDirectorySet)?.let { addAll(it.srcDirs) }
            add(file("src/${sourceSet.name}/kotlin"))
        }
    }

/**
 * Copies of the project dependencies (including test fixtures) of the [classpath], so the same projects can be
 * requested with the attributes of discovery sources.
 */
private fun Project.projectDependenciesOf(classpath: Configuration): Provider<List<ProjectDependency>> =
    provider {
        classpath.allDependencies
            .withType(ProjectDependency::class.java)
            .map { it.copy() }
    }

/**
 * Exposes the sources of `main` (and of `testFixtures`, when the project has test fixtures) to the discovery of
 * other projects depending on this one, along with the project dependencies of these sources so they are
 * resolved transitively.
 *
 * Gradle doesn't allow resolving another project's configuration directly, so the sources are published as
 * variants with a dedicated usage, the same way `apiElements` publishes compiled classes.
 */
internal fun Project.registerDiscoverySourcesElements() {
    sourceSet(SourceSet.MAIN_SOURCE_SET_NAME)?.let { main ->
        registerDiscoverySourcesElements(main, suffix = "", capability = null)
    }

    sourceSet(TEST_FIXTURES_SOURCE_SET)?.let { testFixtures ->
        // the capability requested by `testFixtures(project(":other"))`, see the `java-test-fixtures` plugin
        registerDiscoverySourcesElements(
            testFixtures,
            suffix = "TestFixtures",
            capability = "$group:$name-test-fixtures:$version",
        )
    }
}

private fun Project.registerDiscoverySourcesElements(
    sourceSet: SourceSet,
    suffix: String,
    capability: String?,
) {
    val name = DISCOVERY_SOURCES_ELEMENTS + suffix
    if (configurations.findByName(name) != null) {
        return
    }

    val compileClasspath = configurations.getByName(sourceSet.compileClasspathConfigurationName)
    val dependencies =
        configurations.dependencyScope("$name$DEPENDENCIES_SUFFIX") {
            description = "Projects the '${sourceSet.name}' sources depend on, used by test discovery"
            this.dependencies.addAllLater(projectDependenciesOf(compileClasspath))
        }

    configurations.consumable(name) {
        description = "Sources of '${sourceSet.name}', used by test discovery of dependent projects"
        extendsFrom(dependencies.get())
        attributes.attribute(
            Usage.USAGE_ATTRIBUTE,
            objects.named(Usage::class.java, DISCOVERY_SOURCES_USAGE),
        )
        capability?.let { outgoing.capability(it) }
        // Source directories, never compiled outputs, so discovery never needs compilation
        outgoing.artifacts(sourceDirectories(sourceSet))
    }
}

/**
 * Sources of the projects the [testSourceSet] depends on (transitively), e.g. `main` of `project(":other")`
 * or `testFixtures` of `testFixtures(project(":other"))`.
 *
 * The view is lenient, so dependencies that can't provide sources (e.g. projects without the Java plugin)
 * degrade symbol resolution instead of failing discovery.
 */
internal fun Project.dependencySources(testSourceSet: SourceSet): FileTree {
    val sources =
        configurations.findByName(DISCOVERY_SOURCES) ?: run {
            val compileClasspath = configurations.getByName(testSourceSet.compileClasspathConfigurationName)
            val dependencies =
                configurations.dependencyScope(DISCOVERY_SOURCES_DEPENDENCIES) {
                    description = "Projects the '${testSourceSet.name}' sources depend on, used by test discovery"
                    this.dependencies.addAllLater(projectDependenciesOf(compileClasspath))
                }

            configurations
                .resolvable(DISCOVERY_SOURCES) {
                    description =
                        "Sources of the projects the '${testSourceSet.name}' sources depend on, used by test discovery"
                    extendsFrom(dependencies.get())
                    attributes.attribute(
                        Usage.USAGE_ATTRIBUTE,
                        objects.named(Usage::class.java, DISCOVERY_SOURCES_USAGE),
                    )
                }.get()
        }

    val directories =
        sources
            .incoming
            .artifactView {
                isLenient = true
                componentFilter { it is ProjectComponentIdentifier }
            }.files

    return directories.asFileTree.matching { include(SOURCE_PATTERNS) }
}

/**
 * External library artifacts (jars) of the compile classpath of the [testSourceSet].
 *
 * Project dependencies (e.g. `project(":base")`, `testFixtures(project(":base"))`) and the
 * output of `main` are intentionally excluded: their sources are part of the discovery session
 * already, and including their outputs would force compilation before discovery.
 *
 * The view is lenient so unresolvable dependencies degrade symbol resolution instead of failing discovery.
 */
internal fun Project.testCompileLibraries(testSourceSet: SourceSet): FileCollection =
    configurations
        .getByName(testSourceSet.compileClasspathConfigurationName)
        .incoming
        .artifactView {
            isLenient = true
            componentFilter { it is ModuleComponentIdentifier }
        }.files

/**
 * The JDK home of this project's Java toolchain (the current JVM when no toolchain is configured),
 * falling back to the JVM running Gradle.
 */
internal fun Project.discoveryJdkHome(): Provider<String> {
    val gradleJavaHome = provider { System.getProperty("java.home") }
    val java = extensions.findByType(JavaPluginExtension::class.java)
    val toolchains = extensions.findByType(JavaToolchainService::class.java)

    return if (java != null && toolchains != null) {
        toolchains
            .launcherFor(java.toolchain)
            .map { it.metadata.installationPath.asFile.absolutePath }
            .orElse(gradleJavaHome)
    } else {
        gradleJavaHome
    }
}
