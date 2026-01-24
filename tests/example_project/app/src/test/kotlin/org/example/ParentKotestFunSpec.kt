package org.example

import io.kotest.core.spec.style.FunSpec

abstract class ParentKotestFunSpec(
    body: ParentKotestFunSpec.() -> Unit,
) : FunSpec() {
    init {
        body()
    }
}
