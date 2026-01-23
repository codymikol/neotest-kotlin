package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.determinePosition
import io.kotest.core.spec.style.AnnotationSpec
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import org.jetbrains.kotlin.util.isAnnotated
import kotlin.collections.orEmpty

/**
 * [docs](https://kotest.io/docs/5.9.x/framework/testing-styles.html#annotation-spec)
 */
internal object KotestAnnotationSpecDiscoverer : KotestClassBodyTestTypeDiscoverer {
    override fun canHandle(superType: KtSuperTypeListEntry): Boolean =
        superType.getAllSuperClasses().any { fqn ->
            fqn == FqName("io.kotest.core.spec.style.AnnotationSpec")
        }

    override fun discoverTests(
        body: KtClassBody?,
        classFqn: String,
    ): Set<Discovered> =
        body
            ?.functions
            ?.filter { it.isAnnotated }
            ?.mapNotNull { func ->
                if (func.annotationEntries.none { annotation -> annotation.shortName?.identifier == "Test" } ||
                    func.annotationEntries.any { annotation -> annotation.shortName?.identifier == "Ignore" }
                ) {
                    return@mapNotNull null
                }

                val id = func.name ?: return@mapNotNull null

                Discovered.Test(
                    id = "$classFqn::$id",
                    name = id,
                    position = func.determinePosition(),
                )
            }?.toSet()
            .orEmpty()
}
