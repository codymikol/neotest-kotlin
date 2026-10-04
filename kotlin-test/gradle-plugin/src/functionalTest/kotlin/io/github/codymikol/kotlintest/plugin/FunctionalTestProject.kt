package io.github.codymikol.kotlintest.plugin

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import java.io.File
import java.util.Properties

/**
 * Versions used by the generated projects. They are resolved by the TestKit builds from Maven Central.
 */
object Versions {
    const val KOTLIN = "2.2.21"
    const val KOTEST = "6.1.2"
    const val JUNIT = "5.13.4"

    /**
     * A Kotest version the runner does not support.
     */
    const val UNSUPPORTED_KOTEST = "6.0.7"
}

/**
 * A Gradle build generated in [dir], to which the plugin is applied the way users apply it: through the
 * init script of the repository, with the plugin under test on the classpath of the init script.
 *
 * Applying the plugin from the init script means its classes are loaded by the init script classloader,
 * which doesn't see plugins applied by the build, such as the Kotlin Gradle plugin.
 */
class FunctionalTestProject(val dir: File) {
    companion object {
        /**
         * Written by the `pluginUnderTestMetadata` task, holds the runtime classpath of the plugin under test.
         */
        private const val PLUGIN_METADATA = "plugin-under-test-metadata.properties"

        private const val PUBLISHED_PLUGIN_DEPENDENCY = "classpath(\"io.github.codymikol:kotlin-test:1.0.0\")"

        /**
         * Gradle version to run the builds with, see `functionalTestGradleVersion` in `build.gradle.kts`.
         */
        val gradleVersion: String =
            System.getProperty("kotlintest.functionalTest.gradleVersion") ?: GradleVersion.current().version

        private val pluginClasspath: List<File> by lazy {
            val metadata =
                checkNotNull(FunctionalTestProject::class.java.getResourceAsStream("/$PLUGIN_METADATA")) {
                    "$PLUGIN_METADATA is missing, run the tests through the functionalTest task"
                }
            val properties = metadata.use { Properties().apply { load(it) } }
            properties
                .getProperty("implementation-classpath")
                .split(File.pathSeparator)
                .filter { it.isNotBlank() }
                .map(::File)
        }

        /**
         * The init script of the repository, loading the plugin under test instead of the published one.
         */
        private val initScript: String by lazy {
            val path =
                checkNotNull(System.getProperty("kotlintest.functionalTest.initScript")) {
                    "kotlintest.functionalTest.initScript is not set, run the tests through the functionalTest task"
                }
            val script = File(path).readText()
            check(PUBLISHED_PLUGIN_DEPENDENCY in script) {
                "$path no longer declares $PUBLISHED_PLUGIN_DEPENDENCY, update FunctionalTestProject"
            }
            val files = pluginClasspath.joinToString(", ") { "\"${it.invariantSeparatorsPath}\"" }
            script.replace(PUBLISHED_PLUGIN_DEPENDENCY, "classpath(files($files))")
        }
    }

    private val initScriptFile: File
        get() = dir.resolve(".kotlin-test/test-logging.init.gradle.kts")

    init {
        file(".kotlin-test/test-logging.init.gradle.kts", initScript)
        // Compile in the TestKit daemon instead of starting a Kotlin daemon per generated project
        file("gradle.properties", "kotlin.compiler.execution.strategy=in-process")
    }

    /**
     * Writes [content] to [path], relative to the project directory.
     */
    fun file(
        path: String,
        content: String,
    ): File =
        dir.resolve(path).apply {
            parentFile.mkdirs()
            writeText(content.trimIndent() + "\n")
        }

    /**
     * Writes a settings file using a build cache in the project directory.
     */
    fun settings(vararg includes: String) {
        file(
            "settings.gradle.kts",
            """
            rootProject.name = "${dir.name}"
            ${includes.joinToString("\n            ") { "include(\"$it\")" }}

            buildCache {
                local {
                    directory = file("build-cache")
                }
            }
            """,
        )
    }

    private fun runner(arguments: List<String>): GradleRunner =
        GradleRunner
            .create()
            .withProjectDir(dir)
            .withArguments(listOf("-I", initScriptFile.absolutePath, "--stacktrace") + arguments)
            .apply {
                if (gradleVersion != GradleVersion.current().version) {
                    withGradleVersion(gradleVersion)
                }
            }

    fun build(arguments: List<String>): BuildResult = runner(arguments).build()

    fun build(vararg arguments: String): BuildResult = build(arguments.asList())

    fun buildAndFail(vararg arguments: String): BuildResult = runner(arguments.asList()).buildAndFail()
}

fun readJson(file: File): JsonNode = ObjectMapper().readTree(file)

/**
 * Path of the task named [name] of the project at [projectPath], e.g. `:kotlinTestDiscover` or
 * `:app:kotlinTestDiscover`.
 */
fun taskPath(
    name: String,
    projectPath: String = ":",
): String = if (projectPath == ":") ":$name" else "$projectPath:$name"

/**
 * The JSON file the discovery of the Kotlin file at [path] (relative to the root project directory) is written to,
 * see [discoverArguments].
 */
fun FunctionalTestProject.discoveryOutput(path: String): File = dir.resolve("build/kotlinTestDiscover/$path.json")

/**
 * The JSON file `kotlinTestFindTests` of the project at [projectPath] writes the tests discovered in the Kotlin file
 * at [path] (relative to the root project directory) to.
 */
fun FunctionalTestProject.findTestsDiscoveryOutput(
    path: String,
    projectPath: String = ":",
): File =
    dir.resolve("build/kotlinTestFindTests/${projectPath.replace(':', '_')}")
        .resolve(dir.resolve(path).absolutePath.removePrefix("/") + ".json")

/**
 * Arguments running the discovery task of the project at [projectPath] for the Kotlin file at [path], relative to
 * the root project directory, writing to [discoveryOutput] the way the Lua adapter does.
 */
fun FunctionalTestProject.discoverArguments(
    path: String,
    projectPath: String = ":",
): List<String> =
    listOf(
        taskPath("kotlinTestDiscover", projectPath),
        "-DkotlinTestDiscoverFile=${dir.resolve(path).absolutePath}",
        "-DkotlinTestDiscoverOutput=${discoveryOutput(path).absolutePath}",
        "--configuration-cache",
    )

/**
 * Id to type (`TEST`, `CONTAINER`) of every test of a discovery result.
 */
fun JsonNode.discoveredTests(): Map<String, String> =
    buildMap {
        fun visit(tests: JsonNode?) {
            tests?.forEach { test ->
                put(test.get("id").asText(), test.get("type").asText())
                visit(test.get("tests"))
            }
        }
        visit(this@discoveredTests.get("tests"))
    }

/**
 * `className::id` to status type (`SUCCESS`, `FAILURE`, `IGNORED`) of every result written by `kotlinTestExecute`.
 */
fun JsonNode.executionResults(): Map<String, String> =
    associate { result ->
        val id = listOf(result.get("className").asText(), result.get("id").asText()).filter { it.isNotEmpty() }
        id.joinToString("::") to result.get("status").get("type").asText()
    }
