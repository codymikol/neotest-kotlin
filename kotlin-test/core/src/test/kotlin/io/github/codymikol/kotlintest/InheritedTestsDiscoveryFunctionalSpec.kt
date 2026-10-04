package io.github.codymikol.kotlintest

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * Discovery of tests that are not declared in the class itself: inherited from superclasses
 * declared in source and included from Kotest test factories.
 */
class InheritedTestsDiscoveryFunctionalSpec :
    FunSpec({
        val directory = tempdir()

        fun write(path: String, code: String): File = directory
            .resolve(path)
            .apply {
                parentFile.mkdirs()
                writeText(code.trimIndent())
            }

        fun discoverIds(vararg files: File, include: File = files.last()): List<String> {
            val result = discoverWithClasspath(files.toList(), include)
            return result.tests.flatMap { it.idsInOrder() }
        }

        context("Kotest") {
            test("tests of every superclass are discovered in construction order") {
                val bases = write(
                    "kotest/MultiLevel.kt",
                    """
                    package org.example

                    import io.kotest.core.spec.style.FunSpec

                    abstract class Top : FunSpec() {
                        init {
                            test("top") { }
                        }
                    }

                    abstract class Middle : Top() {
                        init {
                            test("middle") { }
                        }
                    }
                    """
                )
                val spec = write(
                    "kotest/BottomSpec.kt",
                    """
                    package org.example

                    class BottomSpec : Middle() {
                        init {
                            test("bottom") { }
                        }
                    }
                    """
                )

                discoverIds(bases, spec) shouldContainExactly listOf(
                    "org.example.BottomSpec",
                    "org.example.BottomSpec::top",
                    "org.example.BottomSpec::middle",
                    "org.example.BottomSpec::bottom",
                )
            }

            test("a body forwarded to the Kotest spec runs before the init blocks of the base spec") {
                val spec = write(
                    "kotest/ForwardingSpec.kt",
                    """
                    package org.example

                    import io.kotest.core.spec.style.FunSpec

                    abstract class Forwarding(body: FunSpec.() -> Unit) : FunSpec(body) {
                        init {
                            test("after body") { }
                        }
                    }

                    class ForwardingSpec : Forwarding({
                        test("own") { }
                    })
                    """
                )

                discoverIds(spec) shouldContainExactly listOf(
                    "org.example.ForwardingSpec",
                    "org.example.ForwardingSpec::own",
                    "org.example.ForwardingSpec::after body",
                )
            }

            test("a body passed as named argument is discovered where it is invoked") {
                val spec = write(
                    "kotest/NamedSpec.kt",
                    """
                    package org.example

                    import io.kotest.core.spec.style.FunSpec

                    abstract class Named(label: String, body: Named.() -> Unit) : FunSpec() {
                        init {
                            test("before") { }
                            this.body()
                            test("after") { }
                        }
                    }

                    class NamedSpec : Named(body = { test("own") { } }, label = "label")
                    """
                )

                discoverIds(spec) shouldContainExactly listOf(
                    "org.example.NamedSpec",
                    "org.example.NamedSpec::before",
                    "org.example.NamedSpec::own",
                    "org.example.NamedSpec::after",
                )
            }

            test("a body that is not invoked statically is still discovered") {
                val spec = write(
                    "kotest/AppliedSpec.kt",
                    """
                    package org.example

                    import io.kotest.core.spec.style.FunSpec

                    abstract class Applied(body: Applied.() -> Unit) : FunSpec() {
                        init {
                            test("inherited") { }
                            apply(body)
                        }
                    }

                    class AppliedSpec : Applied({
                        test("own") { }
                    })
                    """
                )

                discoverIds(spec) shouldContainExactly listOf(
                    "org.example.AppliedSpec",
                    "org.example.AppliedSpec::inherited",
                    "org.example.AppliedSpec::own",
                )
            }

            test("tests of a concrete superclass are discovered for both classes") {
                val spec = write(
                    "kotest/OpenSpec.kt",
                    """
                    package org.example

                    import io.kotest.core.spec.style.FunSpec

                    open class OpenSpec : FunSpec({
                        test("open") { }
                    })

                    class OpenSubclassSpec : OpenSpec()
                    """
                )

                discoverIds(spec) shouldContainExactly listOf(
                    "org.example.OpenSpec",
                    "org.example.OpenSpec::open",
                    "org.example.OpenSubclassSpec",
                    "org.example.OpenSubclassSpec::open",
                )
            }

            test("test factories are discovered in place of the include") {
                val factories = write(
                    "kotest/Factories.kt",
                    """
                    package org.example

                    import io.kotest.core.factory.TestFactory
                    import io.kotest.core.spec.style.describeSpec
                    import io.kotest.core.spec.style.funSpec
                    import io.kotest.core.spec.style.stringSpec

                    fun blockBodyFactory(): TestFactory {
                        val unused = 1
                        return describeSpec {
                            describe("describe") {
                                it("it") { }
                            }
                        }
                    }

                    object Factories {
                        val strings = stringSpec {
                            "string" { }
                        }
                    }

                    val outer = funSpec {
                        test("outer") { }
                        include(inner)
                    }

                    val inner = funSpec {
                        test("inner") { }
                    }

                    val recursive: TestFactory = funSpec {
                        test("recursive") { }
                        include(recursive)
                    }
                    """
                )
                val spec = write(
                    "kotest/FactorySpec.kt",
                    """
                    package org.example

                    import io.kotest.core.factory.TestFactory
                    import io.kotest.core.spec.style.FunSpec
                    import io.kotest.core.spec.style.funSpec

                    class FactorySpec : FunSpec({
                        test("first") { }
                        include(funSpec { test("inline") { } })
                        include(blockBodyFactory())
                        include(Factories.strings)
                        include("prefix", outer)
                        include(recursive)
                        include(listOf<TestFactory>().first())
                        test("last") { }
                    })
                    """
                )

                discoverIds(factories, spec) shouldContainExactly listOf(
                    "org.example.FactorySpec",
                    "org.example.FactorySpec::first",
                    "org.example.FactorySpec::inline",
                    "org.example.FactorySpec::describe",
                    "org.example.FactorySpec::describe::it",
                    "org.example.FactorySpec::string",
                    "org.example.FactorySpec::prefix outer",
                    "org.example.FactorySpec::prefix inner",
                    "org.example.FactorySpec::recursive",
                    "org.example.FactorySpec::last",
                )
            }

            test("a factory included by a base spec") {
                val spec = write(
                    "kotest/IncludingBaseSpec.kt",
                    """
                    package org.example

                    import io.kotest.core.spec.style.FunSpec
                    import io.kotest.core.spec.style.funSpec

                    val shared = funSpec {
                        test("shared") { }
                    }

                    abstract class IncludingBase : FunSpec() {
                        init {
                            include(shared)
                        }
                    }

                    class IncludingBaseSpec : IncludingBase()
                    """
                )

                discoverIds(spec) shouldContainExactly listOf(
                    "org.example.IncludingBaseSpec",
                    "org.example.IncludingBaseSpec::shared",
                )
            }
        }

        context("JUnit") {
            test("test methods of every superclass are discovered for the concrete class only") {
                val bases = write(
                    "junit/Bases.kt",
                    """
                    package org.example

                    import org.junit.jupiter.api.Test

                    abstract class TopTest<T> {
                        @Test
                        fun top() { }

                        @Test
                        open fun overriddenWithTest() { }

                        @Test
                        open fun overriddenWithoutTest() { }
                    }

                    abstract class MiddleTest : TopTest<String>() {
                        @Test
                        fun middle() { }
                    }
                    """
                )
                val spec = write(
                    "junit/BottomTest.kt",
                    """
                    package org.example

                    import org.junit.jupiter.api.Test

                    class BottomTest : MiddleTest() {
                        @Test
                        fun bottom() { }

                        @Test
                        override fun overriddenWithTest() { }

                        override fun overriddenWithoutTest() { }
                    }
                    """
                )

                val result = discoverWithClasspath(listOf(bases, spec), spec)
                val ids = result.tests.flatMap { it.idsInOrder() }

                ids.toSet() shouldBe setOf(
                    "org.example.BottomTest",
                    "org.example.BottomTest::bottom",
                    "org.example.BottomTest::overriddenWithTest",
                    "org.example.BottomTest::middle",
                    "org.example.BottomTest::top",
                )
                val overriding = result.byId().getValue("org.example.BottomTest::overriddenWithTest")
                File(overriding.position.filename).name shouldBe "BottomTest.kt"

                discoverWithClasspath(listOf(bases, spec), bases).tests shouldHaveSize 0
            }
        }
    })
