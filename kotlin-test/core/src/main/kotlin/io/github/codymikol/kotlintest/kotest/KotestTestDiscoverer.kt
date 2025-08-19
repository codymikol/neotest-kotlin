package io.github.codymikol.kotlintest.kotest

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import io.github.codymikol.kotlintest.TestDiscoverer
import io.github.codymikol.kotlintest.kotest.spec.FunSpecDiscoverer
import io.kotest.core.spec.Spec

public object KotestTestDiscoverer : TestDiscoverer {
    private val supportedSpecs: List<KotestSpecDiscoverer> = listOf(
        FunSpecDiscoverer
    )

    override fun discover(kotlinClass: KSClassDeclaration): List<String> {
        val superTypes = kotlinClass.getAllSuperTypes()
        if (superTypes.none { it.declaration.qualifiedName?.asString() == Spec::class.qualifiedName }) {
            return emptyList()
        }

        supportedSpecs
            .filter { superTypes.any(it::supportsSpec) }
            .forEach { kotlinClass.accept(it, Unit) }

        return emptyList()
    }
}

public abstract class KotestSpecDiscoverer : KSVisitorVoid() {
    public abstract fun supportsSpec(type: KSType): Boolean

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        super.visitFunctionDeclaration(function, data)

        this.discover(function)
    }

    public abstract fun discover(node: KSNode): List<String>
}