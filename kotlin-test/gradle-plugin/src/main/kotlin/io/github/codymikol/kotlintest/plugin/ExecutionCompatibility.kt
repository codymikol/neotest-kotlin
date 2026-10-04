package io.github.codymikol.kotlintest.plugin

import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

/**
 * Version requirements of the test runner, which executes tests with the project's own
 * Kotest, JUnit Platform and kotlin-stdlib. See the "Compatibility" section of the README.
 */
internal object ExecutionCompatibility {
    const val MINIMUM_KOTEST_VERSION = "6.1.0"
    private const val MINIMUM_KOTEST_MAJOR = 6
    private const val MINIMUM_KOTEST_MINOR = 1

    private const val COMPATIBILITY_URL = "https://github.com/codymikol/neotest-kotlin#compatibility"

    private val KOTEST_ENGINE_MODULES =
        listOf(
            "io.kotest:kotest-framework-engine-jvm",
            "io.kotest:kotest-framework-engine",
        )

    private const val JUNIT_PLATFORM_LAUNCHER = "org.junit.platform:junit-platform-launcher"
    private val JUNIT_PLATFORM_MODULES =
        listOf(
            "org.junit.platform:junit-platform-engine",
            "org.junit.platform:junit-platform-commons",
        )

    private val VERSION_REGEX = "^(\\d+)\\.(\\d+)".toRegex()

    /**
     * The `junit-platform-launcher` dependency notation to add to the execution classpath, aligned with
     * the JUnit Platform version of the project, or `null` if the project already has the launcher or
     * doesn't use the JUnit Platform at all.
     *
     * @param modules `group:name` to version of every module on the test runtime classpath.
     */
    @Suppress("ReturnCount")
    fun junitPlatformLauncherFor(modules: Map<String, String>): String? {
        if (JUNIT_PLATFORM_LAUNCHER in modules) {
            return null
        }

        val platformVersion = JUNIT_PLATFORM_MODULES.firstNotNullOfOrNull { modules[it] } ?: return null

        return "$JUNIT_PLATFORM_LAUNCHER:$platformVersion"
    }

    /**
     * An error message if the project uses a Kotest version that the runner does not support,
     * otherwise `null`.
     *
     * @param modules `group:name` to version of every module on the test runtime classpath.
     */
    @Suppress("ReturnCount")
    fun unsupportedKotestMessage(
        projectPath: String,
        modules: Map<String, String>,
    ): String? {
        val (module, version) =
            KOTEST_ENGINE_MODULES.firstNotNullOfOrNull { module -> modules[module]?.let { module to it } }
                ?: return null

        val (major, minor) =
            VERSION_REGEX.find(version)?.destructured?.let { (major, minor) -> major.toInt() to minor.toInt() }
                // unknown version scheme, let the runner try
                ?: return null

        if (major > MINIMUM_KOTEST_MAJOR || (major == MINIMUM_KOTEST_MAJOR && minor >= MINIMUM_KOTEST_MINOR)) {
            return null
        }

        return "neotest-kotlin requires Kotest $MINIMUM_KOTEST_VERSION or newer to run tests, but project " +
            "'$projectPath' uses $module:$version. Please upgrade Kotest, see $COMPATIBILITY_URL"
    }
}

/**
 * Collects `group:name` to version of all modules in the dependency graph of this component.
 */
internal fun ResolvedComponentResult.moduleVersions(): Map<String, String> {
    val visited = mutableSetOf<ResolvedComponentResult>()
    val queue = ArrayDeque(listOf(this))
    val modules = mutableMapOf<String, String>()

    while (queue.isNotEmpty()) {
        val component = queue.removeFirst()
        if (!visited.add(component)) {
            continue
        }

        component.moduleVersion?.let { modules["${it.group}:${it.name}"] = it.version }
        component.dependencies
            .filterIsInstance<ResolvedDependencyResult>()
            .forEach { queue.add(it.selected) }
    }

    return modules
}
