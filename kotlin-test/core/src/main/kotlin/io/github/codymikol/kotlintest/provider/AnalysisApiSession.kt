package io.github.codymikol.kotlintest.provider

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModule
import org.jetbrains.kotlin.analysis.api.projectStructure.KaSourceModule
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.KtSourceModuleBuilder
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtLibraryModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSdkModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile

internal sealed interface Analysis {
    @JvmInline
    value class File(val value: java.io.File) : Analysis

    data class VirtualFile(val filename: String, val code: String) : Analysis
}

/**
 * Builds a standalone Kotlin Analysis API session.
 *
 * The session is made of
 * - a `test` source module containing `files`, these are the only files exposed by [kotlinFiles]
 * - a `main` source module containing `mainFiles` (production code), which the `test` module depends on
 * - a `dependencies` source module containing `dependencyFiles`, sources of other projects the tests depend on
 *   (e.g. production code or test fixtures of other Gradle projects), which both `main` and `test` depend on
 * - a library module for every entry of `classpath` (jars or class directories)
 * - an SDK module for `jdkHome`, when provided
 *
 * When `classpath` is empty no real Kotest/JUnit symbols are available, so lightweight stubs
 * are added to the `test` module as a fallback (see [kotestStubImplVirtualFile] and [junitImplVirtualFiles]).
 * When a `classpath` is provided the stubs are not added, since they would conflict with the
 * real declarations.
 */
internal class AnalysisApiSession(
    files: List<Analysis>,
    unitTestMode: Boolean = false,
    mainFiles: List<Analysis> = emptyList(),
    dependencyFiles: List<Analysis> = emptyList(),
    classpath: Collection<java.io.File> = emptyList(),
    jdkHome: java.io.File? = null,
) : AutoCloseable {
    private val disposable = Disposer.newDisposable("neotest-kotlin analysis session")

    private val useStubs: Boolean = classpath.isEmpty()

    private lateinit var testModule: KaSourceModule

    val apiSession = buildStandaloneAnalysisAPISession(
        projectDisposable = disposable,
        unitTestMode = unitTestMode,
    ) {
        val targetPlatform = JvmPlatforms.defaultJvmPlatform

        buildKtModuleProvider {
            platform = targetPlatform

            val binaryDependencies: List<KaModule> = buildList {
                if (jdkHome != null) {
                    add(
                        buildKtSdkModule {
                            platform = targetPlatform
                            libraryName = "jdk"
                            addBinaryRootsFromJdkHome(jdkHome.toPath(), false)
                        }
                    )
                }

                classpath
                    .map { it.absoluteFile }
                    .distinct()
                    .filter { it.exists() }
                    .forEach { entry ->
                        add(
                            buildKtLibraryModule {
                                platform = targetPlatform
                                libraryName = entry.path
                                addBinaryRoot(entry.toPath())
                            }
                        )
                    }
            }

            val dependenciesModule = dependencyFiles
                .takeIf { it.isNotEmpty() }
                ?.let { dependencies ->
                    buildKtSourceModule {
                        platform = targetPlatform
                        moduleName = "dependencies"

                        binaryDependencies.forEach(::addRegularDependency)
                        dependencies.forEach { addAnalysis(it) }
                    }
                }

            val mainModule = mainFiles
                .takeIf { it.isNotEmpty() }
                ?.let { main ->
                    buildKtSourceModule {
                        platform = targetPlatform
                        moduleName = "main"

                        dependenciesModule?.let(::addRegularDependency)
                        binaryDependencies.forEach(::addRegularDependency)
                        main.forEach { addAnalysis(it) }
                    }
                }

            testModule = buildKtSourceModule {
                platform = targetPlatform
                moduleName = "test"

                if (useStubs) {
                    addSourceVirtualFile(kotestStubImplVirtualFile())
                    addSourceVirtualFiles(junitImplVirtualFiles())
                }

                if (mainModule != null) {
                    addRegularDependency(mainModule)
                    // tests can see `internal` declarations of production code
                    addFriendDependency(mainModule)
                }
                dependenciesModule?.let(::addRegularDependency)
                binaryDependencies.forEach(::addRegularDependency)

                files.forEach { addAnalysis(it) }
            }

            binaryDependencies.forEach(::addModule)
            dependenciesModule?.let(::addModule)
            mainModule?.let(::addModule)
            addModule(testModule)
        }
    }

    /**
     * All [KtFile]s of the `test` source module, excluding stubs.
     */
    internal val kotlinFiles: Set<KtFile> by lazy {
        val stubs = if (useStubs) {
            setOf(KOTEST_STUB_VIRTUAL_FILENAME) + junitImplVirtualFiles().map { it.name }
        } else {
            emptySet()
        }

        apiSession
            .modulesWithFiles[testModule]
            .orEmpty()
            .filterIsInstance<KtFile>()
            // Filter out stubs that aren't real tests
            .filterNot { it.virtualFile is LightVirtualFile && it.name in stubs }
            .toSet()
    }

    /**
     * Releases the project and every file handle (e.g. jars) held by this session.
     * [KtFile]s from [kotlinFiles] must not be used after closing.
     */
    override fun close() {
        Disposer.dispose(disposable)
    }
}

private fun KtSourceModuleBuilder.addAnalysis(analysis: Analysis) {
    when (analysis) {
        is Analysis.File -> addSourceRoot(analysis.value.toPath())
        is Analysis.VirtualFile -> addSourceVirtualFile(
            LightVirtualFile(
                analysis.filename,
                KotlinLanguage.INSTANCE,
                analysis.code
            )
        )
    }
}
