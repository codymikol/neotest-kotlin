package io.github.codymikol.kotlintest

import com.google.devtools.ksp.symbol.KSClassDeclaration

public interface TestDiscoverer {
    public fun discover(kotlinClass: KSClassDeclaration): List<String>
}