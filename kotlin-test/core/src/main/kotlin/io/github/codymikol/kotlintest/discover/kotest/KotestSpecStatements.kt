package io.github.codymikol.kotlintest.discover.kotest

import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclarationWithBody
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.kotlin.psi.KtSuperTypeCallEntry
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.ValueArgument

/*
 * Syntactic helpers of [KotestSpecTests] for statements of spec bodies.
 */

/**
 * Maps the primary constructor parameters of this class to the lambdas passed to them by [call].
 */
internal fun KtClassOrObject.bind(
    call: KtSuperTypeCallEntry,
    bindings: Map<String, KtLambdaExpression>,
): Map<String, KtLambdaExpression> {
    val parameters = this.primaryConstructorParameters.mapNotNull { it.name }

    return call.valueArguments
        .withIndex()
        .mapNotNull { (index, argument) ->
            val parameter = when {
                argument.getArgumentName() != null -> argument.getArgumentName()?.asName?.asString()
                argument is KtLambdaArgument -> parameters.lastOrNull()
                else -> parameters.getOrNull(index)
            }
            val lambda = argument.lambda(bindings)

            if (parameter != null && lambda != null) parameter to lambda else null
        }
        .toMap()
}

/**
 * The lambda passed as this argument, either a lambda literal or a constructor parameter
 * that is bound to a lambda.
 */
internal fun ValueArgument.lambda(bindings: Map<String, KtLambdaExpression>): KtLambdaExpression? =
    when (val expression = this.getArgumentExpression()?.unwrapParentheses()) {
        is KtLambdaExpression -> expression
        is KtNameReferenceExpression -> bindings[expression.getReferencedName()]
        else -> null
    }

/**
 * The lambda bound to a constructor parameter that this statement invokes, e.g. `body()`,
 * `body.invoke()` or `this.body()`.
 */
internal fun KtExpression.invokedLambda(bindings: Map<String, KtLambdaExpression>): KtLambdaExpression? {
    val name = when (this) {
        is KtCallExpression -> (this.calleeExpression as? KtNameReferenceExpression)?.getReferencedName()
        is KtDotQualifiedExpression -> {
            val selector = this.selectorExpression as? KtCallExpression
            val selectorName = (selector?.calleeExpression as? KtNameReferenceExpression)?.getReferencedName()

            when {
                this.receiverExpression is KtThisExpression -> selectorName
                selectorName == "invoke" -> (this.receiverExpression as? KtNameReferenceExpression)
                    ?.getReferencedName()
                else -> null
            }
        }
        else -> null
    }

    return name?.let { bindings[it] }
}

/**
 * This statement as an `include(factory)`, `include(prefix, factory)` or `this.include(...)` call.
 */
internal fun KtExpression.asInclude(): KtCallExpression? {
    val call = when (this) {
        is KtCallExpression -> this
        is KtDotQualifiedExpression -> (this.selectorExpression as? KtCallExpression)
            ?.takeIf { this.receiverExpression is KtThisExpression }
        else -> null
    }

    return call?.takeIf { it.calleeExpression?.text == "include" && it.valueArguments.size in 1..2 }
}

/**
 * The expression returned by this function, `= expression` or the last `return expression`
 * of its block body.
 */
internal fun KtDeclarationWithBody.returnedExpression(): KtExpression? =
    when (val body = this.bodyExpression) {
        is KtBlockExpression -> body.statements.filterIsInstance<KtReturnExpression>().lastOrNull()?.returnedExpression
        else -> body
    }

internal fun KtExpression.unwrapParentheses(): KtExpression? =
    if (this is KtParenthesizedExpression) this.expression?.unwrapParentheses() else this
