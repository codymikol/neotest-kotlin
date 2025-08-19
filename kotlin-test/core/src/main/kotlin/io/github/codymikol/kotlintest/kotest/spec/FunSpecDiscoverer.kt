package io.github.codymikol.kotlintest.kotest.spec

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import io.github.codymikol.kotlintest.kotest.KotestSpecDiscoverer
import io.kotest.core.spec.style.FunSpec

public object FunSpecDiscoverer : KotestSpecDiscoverer() {
    override fun supportsSpec(type: KSType): Boolean = type.declaration.qualifiedName?.asString() == FunSpec::class.qualifiedName

    override fun discover(node: KSNode): List<String> =
        when (node) {
            is KSFunctionDeclaration -> {
                node.simpleName

                emptyList()
            }
            is KSClassDeclaration -> emptyList()
            else -> emptyList()
        }
}