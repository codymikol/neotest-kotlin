package io.github.codymikol.kotlintest

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

object Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        TestDiscoveryProcessor
}

@OptIn(ExperimentalCompilerApi::class)
class TestDiscoveryProcessorFunctionalSpec : FunSpec ({
    test("FunSpec") {
        val source = SourceFile.kotlin("FunSpecExample.kt", """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class FunSpecExample : FunSpec ({
                test("example") {
                    1 shouldBe 1
                }
            })
        """.trimIndent())

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            configureKsp(useKsp2 = true) {
                symbolProcessorProviders += Provider
            }
        }

        compilation.compile().exitCode shouldBe KotlinCompilation.ExitCode.OK
    }
})