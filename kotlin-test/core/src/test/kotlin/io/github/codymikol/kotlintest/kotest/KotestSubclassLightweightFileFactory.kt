package io.github.codymikol.kotlintest.kotest

import com.intellij.testFramework.LightVirtualFile
import io.github.codymikol.kotlintest.provider.Analysis
import org.jetbrains.kotlin.idea.KotlinLanguage

private val mockKotestSubclassApi = """
       package io.kotest.core.spec.style
       
       public class AnnotationSpecSubclass : AnnotationSpec() {}
       public class BehaviorSpecSubclass : BehaviorSpec() {}
       public class DescribeSpecSubclass : DescribeSpec() {}
       public class ExpectSpecSubclass : ExpectSpec() {}
       public class FeatureSpecSubclass : FeatureSpec() {}
       public class FreeSpecSubclass : FreeSpec() {}
       public class FunSpecSubclass : FunSpec() {}
       public class ShouldSpecSubclass : ShouldSpec() {}
       public class StringSpecSubclass : StringSpec() {}
       public class WordSpecSubclass : WordSpec() {}
""".trimIndent()

internal const val KOTEST_STUB_SUBCLASS_VIRTUAL_FILENAME = "KotestSubclass.kt"

internal fun kotestSubclassAnalysis() = Analysis.VirtualFile(
    KOTEST_STUB_SUBCLASS_VIRTUAL_FILENAME,
    mockKotestSubclassApi
)

internal fun kotestSubclassVirtualFile(): LightVirtualFile = LightVirtualFile(
    KOTEST_STUB_SUBCLASS_VIRTUAL_FILENAME,
    KotlinLanguage.INSTANCE,
    mockKotestSubclassApi
)
