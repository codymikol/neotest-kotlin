package io.github.codymikol.kotlintest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

/**
 * Main entry point for the kotlinTest plugin.
 */
public class KotlinTest : CliktCommand() {
    public val classes: List<String> by option(help = "Comma separated fully qualified class names").split(",").required()
    public val filter: String? by option(help = "Filter for a specific namespace/test")
    public val output: File by option(help = "File to write the JSON test results").file().required()

    override fun run() {
        val kotlinClasses = classes.map { className -> Class.forName(className).kotlin }.toSet()
        val report = TestFrameworkRunner.runAll(classes = kotlinClasses, filter = filter)
        val mapper = ObjectMapper().registerKotlinModule()

        mapper.writeValue(output, report)
    }
}

public fun main(args: Array<String>): Unit = KotlinTest().main(args)
