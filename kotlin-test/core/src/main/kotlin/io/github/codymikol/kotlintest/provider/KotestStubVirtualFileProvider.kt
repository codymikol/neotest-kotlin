package io.github.codymikol.kotlintest.provider

import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinLanguage

private val mockKotestApi = """
       package io.kotest.core.spec.style

        class AnnotationSpec
        class BehaviorSpec
        class DescribeSpec
        class ExpectSpec
        class FeatureSpec
        class FreeSpec
        class FunSpec
        class ShouldSpec
        class StringSpec
        class WordSpec
""".trimIndent()

internal fun kotestStubImplVirtualFile(): LightVirtualFile = LightVirtualFile(
    "Kotest.kt",
    KotlinLanguage.INSTANCE,
    mockKotestApi
)
