package io.github.codymikol.kotlintest.execute

import java.io.File

/**
 * Options of the test runner, parsed from arguments of the form `--name=value`.
 *
 * ```
 * --classes=org.example.TestExample,org.example.OtherTest
 * --output=/tmp/output.json
 * --filter=org.example.TestExample::namespace::test
 * ```
 */
internal data class RunnerOptions(
    /**
     * Fully qualified class names to run.
     */
    val classes: List<String>,
    /**
     * File to write the JSON test results to.
     */
    val output: File,
    /**
     * Filter for a specific namespace/test.
     */
    val filter: String?,
) {
    companion object {
        fun parse(args: Array<String>): RunnerOptions {
            val options =
                args.associate { arg ->
                    require(arg.startsWith("--") && "=" in arg) {
                        "Unexpected argument '$arg', expected the form --name=value"
                    }
                    arg.removePrefix("--").substringBefore("=") to arg.substringAfter("=")
                }

            val unknown = options.keys - setOf("classes", "output", "filter")
            require(unknown.isEmpty()) { "Unknown options: ${unknown.joinToString()}" }

            return RunnerOptions(
                classes =
                requireNotNull(options["classes"]) { "Missing option --classes" }
                    .split(",")
                    .filter { it.isNotBlank() },
                output = File(requireNotNull(options["output"]) { "Missing option --output" }),
                filter = options["filter"]?.takeIf { it.isNotEmpty() },
            )
        }
    }
}
