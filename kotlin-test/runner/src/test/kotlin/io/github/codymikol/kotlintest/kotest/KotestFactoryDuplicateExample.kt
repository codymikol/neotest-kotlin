package io.github.codymikol.kotlintest.kotest

import io.kotest.core.spec.style.FunSpec

/**
 * Fixture that includes the same test factory twice, which Kotest runs as `factory test`
 * and `(1) factory test`.
 */
class KotestFactoryDuplicateExample :
    FunSpec({
        include(sharedFactory())
        include(sharedFactory())
    })
