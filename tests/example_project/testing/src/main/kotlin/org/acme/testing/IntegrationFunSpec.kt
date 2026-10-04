package org.acme.testing

import io.kotest.core.spec.style.FunSpec

/**
 * Base spec living in another Gradle project, specs extending it should be discovered.
 *
 * Abstract specs are never discovered. It lives outside the `org.example` package so running
 * `org.example` tests doesn't select it in this project.
 */
abstract class IntegrationFunSpec(
    body: IntegrationFunSpec.() -> Unit,
) : FunSpec() {
    init {
        body()
    }
}
