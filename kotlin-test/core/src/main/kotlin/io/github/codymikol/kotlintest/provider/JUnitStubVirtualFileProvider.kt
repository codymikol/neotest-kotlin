package io.github.codymikol.kotlintest.provider

import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinLanguage

/**
 * [reference](https://docs.junit.org/6.0.0/api/org.junit.jupiter.params/org/junit/jupiter/params/package-summary.html)
 */
private val junitParamsApi = """
    package org.junit.jupiter.params
    
    annotation class ParameterizedTest
""".trimIndent()

/**
 * [reference](https://docs.junit.org/6.0.0/api/org.junit.jupiter.api/org/junit/jupiter/api/package-summary.html)
 */
private val junitApi = """
    package org.junit.jupiter.api
    
    annotation class DisplayName
    annotation class Disabled
    annotation class Nested
    annotation class Test
    annotation class RepeatedTest
    annotation class TestFactory
    annotation class TestTemplate
""".trimIndent()

internal fun junitImplVirtualFiles(): List<LightVirtualFile> = listOf(
    LightVirtualFile(
        "JUnitParamsApi.kt",
        KotlinLanguage.INSTANCE,
        junitParamsApi
    ),
    LightVirtualFile(
        "JUnitApi.kt",
        KotlinLanguage.INSTANCE,
        junitApi
    )
)
