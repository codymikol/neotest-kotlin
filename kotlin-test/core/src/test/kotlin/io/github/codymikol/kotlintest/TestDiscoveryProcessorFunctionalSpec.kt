package io.github.codymikol.kotlintest

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

@OptIn(ExperimentalCompilerApi::class)
object TestCompilerPluginRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        FirExtensionRegistrarAdapter.registerExtension(TestDiscoveryRegistrar)
    }
}

object TestDiscoveryRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::TestDiscoveryExtension
    }
}

@OptIn(ExperimentalCompilerApi::class)
class TestDiscoveryProcessorFunctionalSpec : FunSpec ({
    test("complicated test") {
        val source = SourceFile.kotlin("FunSpecExample.kt", """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class FunSpecExample : FunSpec ({
                context("namespace 1") {
                    test("example") {
                        1 shouldBe 1
                    }

                    context("namespace 2") {
                        test("example") {
                            1 shouldBe 1
                        }

                        context("namespace 3") {
                            test("example") {
                                1 shouldBe 1
                            }
                        }
                    }
                }
                
                test("example") {
                    1 shouldBe 1
                }
            })
        """.trimIndent())

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            supportsK2 = true
            compilerPluginRegistrars = listOf(TestCompilerPluginRegistrar)
        }

        compilation.compile().exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    test("top-level test") {
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
            supportsK2 = true
            compilerPluginRegistrars = listOf(TestCompilerPluginRegistrar)
        }

        compilation.compile().exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    test("single nested test") {
        val source = SourceFile.kotlin("FunSpecExample.kt", """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class FunSpecExample : FunSpec ({
                context("namespace") {
                    test("example") {
                        1 shouldBe 1
                    }
                }
            })
        """.trimIndent())

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            supportsK2 = true
            compilerPluginRegistrars = listOf(TestCompilerPluginRegistrar)
        }

        compilation.compile().exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    test("deeply nested test") {
        val source = SourceFile.kotlin("FunSpecExample.kt", """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class FunSpecExample : FunSpec ({
                context("namespace 1") {
                    context("namespace 2") {
                        context("namespace 3") {
                            context("namespace 4") {
                                test("example") {
                                    1 shouldBe 1
                                }
                            }
                        }
                    }
                }
            })
        """.trimIndent())

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            supportsK2 = true
            compilerPluginRegistrars = listOf(TestCompilerPluginRegistrar)
        }

        compilation.compile().exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    test("multiple top-level tests") {
        val source = SourceFile.kotlin("FunSpecExample.kt", """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class FunSpecExample : FunSpec ({
                test("example 1") {
                    1 shouldBe 1
                }

                test("example 2") {
                    1 shouldBe 1
                }

                test("example 3") {
                    1 shouldBe 1
                }
            })
        """.trimIndent())

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            supportsK2 = true
            compilerPluginRegistrars = listOf(TestCompilerPluginRegistrar)
        }

        compilation.compile().exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    test("top-level test - fails to compile before test") {
        val source = SourceFile.kotlin("FunSpecExample.kt", """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class FunSpecExample : FunSpec ({
                test()

                test("example") {}
            })
        """.trimIndent())

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            supportsK2 = true
            compilerPluginRegistrars = listOf(TestCompilerPluginRegistrar)
        }

        compilation.compile().exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
    }

    test("top-level test - fails to compile after test") {
        val source = SourceFile.kotlin("FunSpecExample.kt", """
            import io.kotest.core.spec.style.FunSpec
            import io.kotest.matchers.shouldBe
            
            class FunSpecExample : FunSpec ({
                test("example") {}

                test()
            })
        """.trimIndent())

        val compilation = KotlinCompilation().apply {
            sources = listOf(source)
            inheritClassPath = true
            supportsK2 = true
            compilerPluginRegistrars = listOf(TestCompilerPluginRegistrar)
        }

        compilation.compile().exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
    }
})