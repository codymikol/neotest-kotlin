import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import io.github.codymikol.kotlintest.files.IsTestContainingFileExecutor
import io.github.codymikol.kotlintest.files.model.FileCommandResult
import io.github.codymikol.kotlintest.provider.Analysis
import io.github.codymikol.kotlintest.provider.AnalysisApiSession
import java.io.File

public class FindTestFiles : CliktCommand() {

    private val isTestContainingFileExecutor = IsTestContainingFileExecutor()

    private val mapper by lazy { ObjectMapper().registerKotlinModule() }

    private val files: List<File> by option(
        help = "Comma separated test source absolute paths"
    ).file(canBeDir = false).split(",").required()

    private val output: File by option(help = "File to write the JSON test results").file().required()

    override fun run() {
        val session = AnalysisApiSession(
            files = files.map { Analysis.File(it) },
            unitTestMode = false
        )

        val ktFiles = session.kotlinFiles

        val testFileResults = ktFiles.map(isTestContainingFileExecutor::getTestFileResult)

        val fileCommandResult = FileCommandResult(testFileResults = testFileResults)

        mapper.writeValue(output, fileCommandResult)
    }
}
