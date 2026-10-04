package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.MAX_RESOLUTION_DEPTH
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.resolveSourceDeclaration
import io.github.codymikol.kotlintest.discover.sourceSuperclass
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtSuperTypeCallEntry

/**
 * Package of Kotest itself, its spec classes are treated as binaries even when (stubbed) sources exist.
 */
private const val KOTEST_PACKAGE = "io.kotest."

/**
 * The lambda passed to a test factory builder, e.g. `funSpec { ... }`, and the discoverer of its style.
 */
private data class Factory(
    val lambda: KtLambdaExpression,
    val discoverer: KotestExpressionTestTypeDiscoverer,
)

/**
 * Collects the root tests of a concrete Kotest spec in the order in which Kotest registers them
 * when constructing the spec:
 *
 * 1. the tests of its superclasses declared in source (e.g. an abstract base spec of the project
 *    with tests in its `init` blocks), where lambdas passed to the constructor of a base spec are
 *    discovered where the base spec invokes them (`body()`),
 * 2. lambdas passed to a Kotest spec (`FunSpec({ ... })`) or to a superclass that is only
 *    available as a binary (e.g. from a jar), which invoke them during construction,
 * 3. the tests of the `init` blocks of the spec itself.
 *
 * Statically resolvable [test factories](https://kotest.io/docs/framework/test-factories.html)
 * included with `include(factory)`/`include(prefix, factory)` are discovered in place of the
 * `include` call, as Kotest registers their tests as root tests of the spec.
 *
 * Positions of inherited and included tests point at their declaration, which may be in a file
 * other than the one of the spec.
 */
internal class KotestSpecTests(
    private val classFqn: String,
    private val specDiscoverer: KotestExpressionTestTypeDiscoverer,
) {
    /**
     * Lambdas passed to superclasses declared in source, and whether they have been discovered,
     * see [collect].
     */
    private val boundLambdas: MutableMap<KtLambdaExpression, Boolean> = linkedMapOf()

    /**
     * Factories currently being discovered, guards against factories that include themselves.
     */
    private val factoryStack: MutableSet<KtLambdaExpression> = mutableSetOf()

    /**
     * Root tests (and containers) of [spec], in registration order. Unlike a [Set], duplicates
     * are retained, e.g. when a factory is included twice, as Kotest registers (and renames) both.
     */
    fun collect(spec: KtClassOrObject): List<Discovered> {
        val tests = construct(spec, emptyMap(), depth = 0)

        // lambdas passed to a base spec that are never invoked statically (e.g. `apply(body)`) are
        // most likely invoked anyway, discovering them is better than losing them.
        val notInvoked = boundLambdas
            .filterValues { discovered -> !discovered }
            .keys
            .flatMap { lambda -> statements(lambda.bodyExpression, specDiscoverer, emptyMap(), prefix = null) }

        return tests + notInvoked
    }

    /**
     * Tests registered when constructing [klass], where [bindings] maps constructor parameters
     * of [klass] to the lambdas passed by its subclass.
     */
    private fun construct(
        klass: KtClassOrObject,
        bindings: Map<String, KtLambdaExpression>,
        depth: Int,
    ): List<Discovered> {
        val superCall = klass.superTypeListEntries.filterIsInstance<KtSuperTypeCallEntry>().firstOrNull()
        val superclass = klass
            .takeIf { depth < MAX_RESOLUTION_DEPTH }
            ?.sourceSuperclass()
            ?.takeUnless { it.fqName?.asString().orEmpty().startsWith(KOTEST_PACKAGE) }

        val superclassTests = if (superclass != null) {
            val superBindings = superCall
                ?.let { call -> superclass.bind(call, bindings) }
                .orEmpty()
            superBindings.values.forEach { lambda -> boundLambdas.putIfAbsent(lambda, false) }

            construct(superclass, superBindings, depth + 1)
        } else {
            // a Kotest spec or a base spec from a binary invokes the lambdas passed to it
            superCall
                ?.valueArguments
                .orEmpty()
                .mapNotNull { argument -> argument.lambda(bindings) }
                .flatMap { lambda -> invoke(lambda) }
        }

        val initBlockTests = klass
            .body
            ?.anonymousInitializers
            .orEmpty()
            .flatMap { initializer -> statements(initializer.body, specDiscoverer, bindings, prefix = null) }

        return superclassTests + initBlockTests
    }

    private fun invoke(lambda: KtLambdaExpression): List<Discovered> {
        boundLambdas[lambda] = true
        return statements(lambda.bodyExpression, specDiscoverer, emptyMap(), prefix = null)
    }

    /**
     * Tests registered by the statements of [block], see [KotestExpressionTestTypeDiscoverer.discoverStatement].
     */
    private fun statements(
        block: KtExpression?,
        discoverer: KotestExpressionTestTypeDiscoverer,
        bindings: Map<String, KtLambdaExpression>,
        prefix: String?,
    ): List<Discovered> =
        block
            ?.children
            ?.filterIsInstance<KtExpression>()
            .orEmpty()
            .flatMap { statement ->
                val invokedLambda = statement.invokedLambda(bindings)
                val include = statement.asInclude()

                when {
                    invokedLambda != null -> invoke(invokedLambda).withPrefix(prefix)
                    include != null -> include(include, prefix)
                    else -> discoverer.discoverStatement(statement, classFqn).withPrefix(prefix)
                }
            }

    /**
     * Tests of the factory included by [include], or nothing when the factory can't be resolved statically.
     *
     * `include(prefix, factory)` registers the root tests of the factory as `<prefix> <name>`.
     */
    private fun include(
        include: KtCallExpression,
        prefix: String?,
    ): List<Discovered> {
        val arguments = include.valueArguments
        val factory = arguments.last().getArgumentExpression()?.resolveFactory(depth = 0)

        if (factory == null || !factoryStack.add(factory.lambda)) {
            return emptyList()
        }

        val includePrefix = arguments.takeIf { it.size == 2 }?.first()?.getArgumentExpression()?.text?.trim('"')

        return try {
            statements(
                factory.lambda.bodyExpression,
                factory.discoverer,
                emptyMap(),
                prefix = listOfNotNull(prefix, includePrefix).joinToString(" ").ifEmpty { null },
            )
        } finally {
            factoryStack.remove(factory.lambda)
        }
    }
}

/**
 * Resolves this expression to a test factory builder call (e.g. `funSpec { }`), following calls
 * to functions and references to properties declared in source.
 */
private fun KtExpression.resolveFactory(depth: Int): Factory? {
    val expression = this.unwrapParentheses()?.takeIf { depth <= MAX_RESOLUTION_DEPTH }
    val call = when (expression) {
        is KtCallExpression -> expression
        is KtQualifiedExpression -> expression.selectorExpression as? KtCallExpression
        else -> null
    }

    return when {
        expression == null -> null
        call != null && call.factoryDiscoverer() != null -> call.factoryLambda()?.let {
            Factory(it, checkNotNull(call.factoryDiscoverer()))
        }
        else -> expression.resolveFactoryDeclaration()?.resolveFactory(depth + 1)
    }
}

/**
 * The expression a function or property declaration this expression refers to returns.
 */
private fun KtExpression.resolveFactoryDeclaration(): KtExpression? =
    when (val declaration = this.resolveSourceDeclaration()) {
        is KtNamedFunction -> declaration.returnedExpression()
        is KtProperty -> declaration.initializer ?: declaration.getter?.returnedExpression()
        else -> null
    }

/**
 * The discoverer of the style of the test factory this call builds, e.g. `funSpec { }`.
 */
private fun KtCallExpression.factoryDiscoverer(): KotestExpressionTestTypeDiscoverer? {
    val name = (this.calleeExpression as? KtNameReferenceExpression)?.getReferencedName()

    return KotestTestDiscoverer.testTypes
        .filterIsInstance<KotestExpressionTestTypeDiscoverer>()
        .firstOrNull { it.factoryBuilder == name }
}

private fun KtCallExpression.factoryLambda(): KtLambdaExpression? =
    this.lambdaArguments.lastOrNull()?.getLambdaExpression()
        ?: this.valueArguments.lastOrNull()?.getArgumentExpression() as? KtLambdaExpression

/**
 * Prefixes the names of these root tests, as done by Kotest's `include(prefix, factory)`.
 */
private fun List<Discovered>.withPrefix(prefix: String?): List<Discovered> =
    if (prefix == null) {
        this
    } else {
        this.map { discovered -> discovered.renamed("$prefix ${discovered.name}") }
    }

/**
 * Renames this root test to [name], updating its id and the ids of all its nested tests.
 */
private fun Discovered.renamed(name: String): Discovered {
    val parentId = this.id.removeSuffix("::${this.name}")

    return this.reparented("$parentId::$name", name)
}

private fun Discovered.reparented(id: String, name: String): Discovered =
    when (this) {
        is Discovered.Test -> copy(id = id, name = name)
        is Discovered.Container -> copy(
            id = id,
            name = name,
            tests = tests.map { it.reparented("$id::${it.name}", it.name) }.toSet(),
        )
    }
