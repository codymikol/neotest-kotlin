package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.model.Discovered
import org.jetbrains.kotlin.psi.KtClassBody

/**
 * Akin to [KotestExpressionTestTypeDiscoverer], but used exclusively for [KotestAnnotationSpecDiscoverer]
 * because it has the form
 *
 * ```
 * class AnnotationSpecExample : AnnotationSpec() {
 *   @Test
 *   fun example() {
 *     1 shouldBe 1
 *   }
 * }
 * ```
 *
 * compared to other Kotest Specs that leverage a lambda expression as the body of the Spec constructor.
 */
internal interface KotestClassBodyTestTypeDiscoverer : KotestTestTypeDiscoverer {
    fun discoverTests(
        body: KtClassBody?,
        classFqn: String,
    ): Set<Discovered>
}
