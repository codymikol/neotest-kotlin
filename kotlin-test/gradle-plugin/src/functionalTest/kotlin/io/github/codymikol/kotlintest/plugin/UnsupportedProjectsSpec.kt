package io.github.codymikol.kotlintest.plugin

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class UnsupportedProjectsSpec : FunSpec({
    test("the plugin doesn't fail builds without Kotlin or a test source set") {
        val project = FunctionalTestProject(tempdir())
        project.settings("java-lib", "docs")
        project.file("build.gradle.kts", "")
        project.file(
            "java-lib/build.gradle.kts",
            """
            plugins {
                `java-library`
            }
            """,
        )
        project.file("java-lib/src/main/java/com/example/Greeter.java", "package com.example; class Greeter {}")
        project.file(
            "docs/build.gradle.kts",
            """
            plugins {
                base
            }
            """,
        )
        // A Kotlin file outside of any Kotlin project
        project.file("docs/snippets/Snippet.kt", "fun snippet() = Unit")

        val result = project.build("tasks", "--all", "--configuration-cache")

        result.output shouldNotContain "kotlinTestExecute"
    }

    test("kotlinTestExecute rejects Kotest versions older than 6.1") {
        val project = FunctionalTestProject(tempdir())
        project.settings()
        project.calculatorProject(buildFile = kotlinJvmBuild(kotest = Versions.UNSUPPORTED_KOTEST))

        val result =
            project.buildAndFail(
                "kotlinTestExecute",
                "-Pclasses=com.example.CalculatorSpec",
                "-PoutputFile=${project.dir.resolve("build/execution.json").absolutePath}",
            )

        result.output shouldContain "neotest-kotlin requires Kotest 6.1.0 or newer to run tests"
        result.output shouldContain "io.kotest:kotest-framework-engine-jvm:${Versions.UNSUPPORTED_KOTEST}"
    }
})
