package io.github.codymikol.kotlintest

import com.intellij.lang.Language
import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.LightVirtualFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.analysis.api.projectStructure.KaSourceModule
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.KtSourceModuleBuilder
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtLibraryModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.cli.common.CliModuleVisibilityManagerImpl
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironment
import org.jetbrains.kotlin.fir.declarations.builder.buildFile
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.konan.file.createTempFile
import org.jetbrains.kotlin.load.kotlin.ModuleVisibilityManager
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile

internal fun createKtFile(filename: String, code: String): KtFile {
    val session = buildStandaloneAnalysisAPISession(unitTestMode = true) {
        buildKtModuleProvider {
            platform = JvmPlatforms.defaultJvmPlatform

            val module = buildKtSourceModule {
                moduleName = "test"
                platform = JvmPlatforms.defaultJvmPlatform
                addSourceVirtualFile(LightVirtualFile(filename, KotlinLanguage.INSTANCE, code))
            }

            addModule(module)
        }
    }

    return session.modulesWithFiles
        .flatMap { it.value }
        .first { it.name == filename } as KtFile
}

class TestDiscoveryProcessorFunctionalSpec : FunSpec ({
    test("top-level test") {
        val ktFile = createKtFile("ExampleFunSpec.kt", """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFunSpec : FunSpec({
                test("test") {
                  1 shouldBe 1
                }
            })
        """.trimIndent())

        val results = KotestTestDiscoverer.discoverTests(ktFile)

        results shouldBe setOf(
            DiscoveredTest(
                id = "test",
                type = TestType.TEST,
                position = Position(
                    filename = "/ExampleFunSpec.kt",
                    start = 5,
                    end = 7
                )
            )
        )
    }

    test("nested test") {
        val ktFile = createKtFile("ExampleFunSpec.kt", """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFunSpec : FunSpec({
                context("container") {
                    test("test") {
                      1 shouldBe 1
                    }
                }
            })
        """.trimIndent())

        val results = KotestTestDiscoverer.discoverTests(ktFile)

        results shouldBe setOf(
            DiscoveredTest(
                id = "container",
                type = TestType.CONTAINER,
                position = Position(
                    filename = "/ExampleFunSpec.kt",
                    start = 5,
                    end = 9
                )
            ),
            DiscoveredTest(
                id = "container::test",
                type = TestType.TEST,
                position = Position(
                    filename = "/ExampleFunSpec.kt",
                    start = 6,
                    end = 8
                )
            )
        )
    }

    test("multiple top-level test") {
        val ktFile = createKtFile("ExampleFunSpec.kt", """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class ExampleFunSpec : FunSpec({
                test("test") {
                  1 shouldBe 1
                }
                
                test("test1") {
                  1 shouldBe 1
                }
                
                test("test2") {
                  1 shouldBe 1
                }
            })
        """.trimIndent())

        val results = KotestTestDiscoverer.discoverTests(ktFile)

        results shouldBe setOf(
            DiscoveredTest(
                id = "test",
                type = TestType.TEST,
                position = Position(
                    filename = "/ExampleFunSpec.kt",
                    start = 5,
                    end = 7
                )
            ),
            DiscoveredTest(
                id = "test1",
                type = TestType.TEST,
                position = Position(
                    filename = "/ExampleFunSpec.kt",
                    start = 9,
                    end = 11
                )
            ),
            DiscoveredTest(
                id = "test2",
                type = TestType.TEST,
                position = Position(
                    filename = "/ExampleFunSpec.kt",
                    start = 13,
                    end = 15
                )
            )
        )
    }
})