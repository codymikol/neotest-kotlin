package io.github.codymikol.kotlintest

import com.intellij.openapi.application.ApplicationManager
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.DiscoveredResult
import io.github.codymikol.kotlintest.provider.Analysis
import io.github.codymikol.kotlintest.provider.AnalysisApiSession
import java.io.File

/**
 * The classpath of these tests, which contains Kotest and JUnit, used to discover with real
 * (rather than stubbed) test framework symbols.
 */
internal val discoveryClasspath: List<File> =
    System.getProperty("java.class.path")
        .split(File.pathSeparator)
        .filter { it.isNotBlank() }
        .map(::File)

internal val discoveryJdkHome = File(System.getProperty("java.home"))

/**
 * Discovers [include] with all [files] as sources, resolving symbols against [discoveryClasspath].
 *
 * Unlike [createKtFile], the session is closed as it holds the entire test classpath. Once a
 * unit test mode session exists in this JVM, disposing requires a write action.
 */
internal fun discoverWithClasspath(files: Collection<File>, include: File? = null): DiscoveredResult {
    val session = AnalysisApiSession(
        files = files.map { Analysis.File(it) },
        unitTestMode = true,
        classpath = discoveryClasspath,
        jdkHome = discoveryJdkHome,
    )

    try {
        val filesToDiscover = session.kotlinFiles
            .filter { include == null || it.virtualFilePath == include.absolutePath }
            .toSet()

        return TestDiscoverer.discoverAllTests(filesToDiscover)
    } finally {
        ApplicationManager.getApplication().runWriteAction { session.close() }
    }
}

/**
 * Ids of this and all its nested tests in tree (definition) order.
 */
internal fun Discovered.idsInOrder(): List<String> =
    listOf(id) + ((this as? Discovered.Container)?.tests.orEmpty().flatMap { it.idsInOrder() })

/**
 * All discovered tests and containers, including the classes, by id.
 */
internal fun DiscoveredResult.byId(): Map<String, Discovered> =
    tests
        .flatMap { listOf(it) + it.allTests() }
        .associateBy { it.id }
