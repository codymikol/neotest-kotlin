package io.github.codymikol.kotlintest.discover.kotest

import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry

internal sealed interface KotestTestTypeDiscoverer {
    /**
     * Whether this [KotestTestTypeDiscoverer] can handle this SuperType
     *
     * e.g., FunSpec
     */
    fun canHandle(superType: KtSuperTypeListEntry): Boolean
}

/**
 * Recursively returns the list of classes and interfaces extended or implemented by the class.
 *
 * [original code](https://github.com/kotest/kotest-intellij-plugin/blob/4f27fa2f057509a72f219eeb5140902603815839/src/main/kotlin/io/kotest/plugin/intellij/psi/superClasses.kt#L9-L24)
 */
internal fun KtSuperTypeListEntry.getAllSuperClasses(): List<FqName> {
    val ref = this.typeReference ?: return emptyList()

    return analyze(this) {
        val kaType = ref.type
        val superTypes = (kaType.allSupertypes(false) + kaType).toList()
        superTypes.mapNotNull {
            val classId = it.symbol?.classId?.takeIf { id -> id != StandardClassIds.Any }
            classId?.asSingleFqName()
        }
    }
}
