package io.github.codymikol.kotlintest

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.codymikol.kotlintest.kotest.KotestTestDiscoverer

/**
 * The entry point for Kotlin Test Discovery.
 */
public object TestDiscoveryProcessor : SymbolProcessor {
    private val testFrameworks: List<TestDiscoverer> = listOf(
        KotestTestDiscoverer
    )

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val tests = resolver.getAllFiles().flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .flatMap { kotlinClass -> testFrameworks.map { it.discover(kotlinClass) } }
            .flatten()
            .toList()

        println(tests)

        return emptyList()
    }
}