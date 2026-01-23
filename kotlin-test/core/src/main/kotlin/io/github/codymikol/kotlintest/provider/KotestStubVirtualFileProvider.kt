package io.github.codymikol.kotlintest.provider

import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.analysis.project.structure.builder.KtModuleBuilder
import org.jetbrains.kotlin.idea.KotlinLanguage

private val mockKotestApi = """
       package io.kotest.core.spec.style
       
       class AnnotationSpec {}
       class AnnotationSpecSubclass : AnnotationSpec
       
       class BehaviorSpec {}
       class BehaviorSpecSubclass : BehaviorSpec
       
       class DescribeSpec {}
       class DescribeSpecSubclass : DescribeSpec
       
       class ExpectSpec {}
       class ExpectSpecSubclass : ExpectSpec
       
       class FeatureSpec {}
       class FeatureSpecSubclass : FeatureSpec
       
       class FreeSpec {}
       class FreeSpecSubclass : FreeSpec
       
       class FunSpec {}
       class FunSpecSubclass : FunSpec
       
       class ShouldSpec {}
       class ShouldSpecSubclass : ShouldSpec
       
       class StringSpec {}
       class StringSpecSubclass : StringSpec
       
       class WordSpec {}
       class WordSpecSubclass : WordSpec
    """.trimIndent()


context(builder: KtModuleBuilder)
internal fun kotestStubImplVirtualFile() : LightVirtualFile = LightVirtualFile(
    "Kotest.kt",
    KotlinLanguage.INSTANCE,
    mockKotestApi
)
