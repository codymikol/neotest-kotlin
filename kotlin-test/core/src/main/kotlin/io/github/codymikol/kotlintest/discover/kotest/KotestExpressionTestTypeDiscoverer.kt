package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.model.Discovered
import org.jetbrains.kotlin.psi.KtExpression

internal interface KotestExpressionTestTypeDiscoverer : KotestTestTypeDiscoverer {
    /**
     * Name of the function that builds a [test factory](https://kotest.io/docs/framework/test-factories.html)
     * of this style, e.g. `funSpec` for `FunSpec`.
     */
    val factoryBuilder: String

    /**
     * Discovers the tests registered by a single [statement] of a spec (or test factory) body,
     * e.g. `test("name") { }` for a `FunSpec`, with ids nested under [parentId].
     */
    fun discoverStatement(
        statement: KtExpression,
        parentId: String,
    ): List<Discovered>

    /**
     * Discovers the tests registered by all statements of [expression], e.g. the body of the
     * lambda passed to the spec constructor.
     */
    fun discoverTests(
        expression: KtExpression?,
        classFqn: String,
    ): Set<Discovered> =
        expression
            ?.children
            ?.filterIsInstance<KtExpression>()
            ?.flatMap { statement -> discoverStatement(statement, classFqn) }
            ?.toSet()
            .orEmpty()
}
