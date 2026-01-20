package io.github.codymikol.kotlintest.provider

import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.idea.KotlinLanguage

private val mockKotestApi = """
       package io.kotest.core.spec.style
       
       public open class AnnotationSpec {}
       public open class BehaviorSpec {}
       public open class DescribeSpec {}
       public open class ExpectSpec {}
       public open class FeatureSpec {}
       public open class FreeSpec {}
       public open class FunSpec {}
       public open class ShouldSpec {}
       public open class StringSpec {}
       public open class WordSpec {}
    """.trimIndent()

internal const val KotestStubVirtualFileName = "Kotest.kt"

internal fun kotestStubImplVirtualFile(): LightVirtualFile = LightVirtualFile(
    KotestStubVirtualFileName,
    KotlinLanguage.INSTANCE,
    mockKotestApi
)
