package io.github.codymikol.kotlintest.discover

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression

/**
 * Upper bound of superclasses/declarations followed, guarding against cycles in (invalid) code.
 */
internal const val MAX_RESOLUTION_DEPTH: Int = 32

/**
 * Whether this declaration is available as source, rather than e.g. decompiled from a jar
 * of the classpath which has no bodies (no `init` blocks, no function bodies).
 */
private fun KtDeclaration.isSource(): Boolean = !this.containingKtFile.isCompiled

/**
 * The superclass (not an interface) of this class or object when it is declared in source,
 * e.g. an abstract base spec in the test or main sources of the project. `null` when the
 * superclass can't be resolved or is only available as a binary (e.g. from a jar).
 */
internal fun KtClassOrObject.sourceSuperclass(): KtClass? {
    val superclass = analyze(this) {
        this@sourceSuperclass
            .classSymbol
            ?.superTypes
            ?.asSequence()
            ?.mapNotNull { superType -> (superType as? KaClassType)?.symbol as? KaClassSymbol }
            ?.firstOrNull { symbol -> symbol.classKind == KaClassKind.CLASS }
            ?.psi
    }

    return (superclass as? KtClass)?.takeIf { it.isSource() && it != this }
}

/**
 * The superclasses of this class or object that are declared in source, starting with the
 * direct superclass, see [sourceSuperclass].
 */
internal fun KtClassOrObject.sourceSuperclasses(): List<KtClass> =
    generateSequence(this.sourceSuperclass()) { it.sourceSuperclass() }
        .take(MAX_RESOLUTION_DEPTH)
        .toList()

/**
 * Resolves the declaration that this call (e.g. `factory()`) or reference (e.g. `factory`,
 * `Factories.factory`) refers to when it is declared in source.
 */
internal fun KtExpression.resolveSourceDeclaration(): KtDeclaration? {
    val reference = when (this) {
        is KtCallExpression -> this.calleeExpression
        is KtQualifiedExpression -> (this.selectorExpression as? KtCallExpression)?.calleeExpression
            ?: this.selectorExpression
        else -> this
    } as? KtReferenceExpression ?: return null

    val declaration: PsiElement? = analyze(reference) {
        reference.mainReference.resolveToSymbol()?.psi
    }

    return (declaration as? KtDeclaration)?.takeIf { it.isSource() }
}
