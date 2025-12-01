package io.github.codymikol.kotlintest.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

public class Discover : CliktCommand() {
    public val absolutePaths: List<String> by option(
        help = "Comma separated absolute paths to files for discovery"
    ).split(",").required()

    public val output: File by option(help = "File to write the JSON test results").file().required()

    override fun run() {
        TODO("Not yet implemented")
    }
}
