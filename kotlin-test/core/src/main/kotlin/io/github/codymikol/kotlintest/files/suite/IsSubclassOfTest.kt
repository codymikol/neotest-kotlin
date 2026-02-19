package io.github.codymikol.kotlintest.files.suite

import com.intellij.psi.util.childrenOfType
import io.github.codymikol.kotlintest.discover.kotest.getAllSuperClasses
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry
import org.jetbrains.kotlin.psi.psiUtil.isAbstract

internal class IsSubclassOfTest(val pkg: String, val identifier: String) {

    private val testFqn = FqName("$pkg.$identifier")

    fun evaluate(kotlinFile: KtFile): Boolean {
        analyze(kotlinFile) {
            val allClasses = kotlinFile.childrenOfType<KtClass>()

            val concreteClasses = allClasses.filterNot(KtClass::isAbstract)

            val superTypes = concreteClasses.flatMap(KtClass::getSuperTypeListEntries)

            return superTypes.any(::canHandle)
        }
    }

    private fun canHandle(superType: KtSuperTypeListEntry): Boolean = superType
        .getAllSuperClasses().any { fqn -> fqn == testFqn }
}
