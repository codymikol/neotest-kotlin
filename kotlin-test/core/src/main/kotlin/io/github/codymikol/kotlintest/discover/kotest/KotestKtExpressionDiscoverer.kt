package io.github.codymikol.kotlintest.discover.kotest

import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.determinePosition
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression

internal sealed class KotestKtExpressionDiscoverer : KotestExpressionTestTypeDiscoverer {
    /**
     * keywords used to represent containers.
     */
    abstract val containers: List<String>

    /**
     * keywords used to represent leaf node tests.
     */
    abstract val tests: List<String>

    /**
     * Whether to suffix duplicate test/container names within the same scope.
     */
    open val disambiguateDuplicateNames: Boolean = false

    private enum class CallType {
        Container,
        Test,
    }

    private data class CallInfo(
        val callExpression: KtCallExpression,
        val name: String,
        val type: CallType,
    )

    private fun KtCallExpression.toCallInfo(): CallInfo? {
        val id =
            this.valueArguments
                .firstOrNull()
                ?.firstChild
                ?.text
                ?.trim('"') ?: return null

        return when (this.calleeExpression?.firstChild?.text) {
            in tests -> CallInfo(this, id, CallType.Test)
            in containers -> CallInfo(this, id, CallType.Container)
            else -> null
        }
    }

    private fun KtExpression.findTests(parentId: String): Set<Discovered> =
        this
            .children
            .filterIsInstance<KtCallExpression>()
            .mapNotNull { callExpression -> callExpression.toCallInfo() }
            .let { calls ->
                if (calls.isEmpty()) {
                    emptySet()
                } else {
                    val nameCounts =
                        if (disambiguateDuplicateNames) {
                            calls.groupingBy { it.name }.eachCount()
                        } else {
                            emptyMap()
                        }
                    val nameIndexes = mutableMapOf<String, Int>()

                    calls
                        .flatMap { callInfo ->
                            val suffix =
                                if (disambiguateDuplicateNames && (nameCounts[callInfo.name] ?: 0) > 1) {
                                    val nextIndex = (nameIndexes[callInfo.name] ?: 0) + 1
                                    nameIndexes[callInfo.name] = nextIndex
                                    "#$nextIndex"
                                } else {
                                    ""
                                }

                            val disambiguatedName = "${callInfo.name}$suffix"
                            val fullId = "$parentId::$disambiguatedName"

                            when (callInfo.type) {
                                CallType.Test ->
                                    listOf(
                                        Discovered.Test(
                                            id = fullId,
                                            name = disambiguatedName,
                                            position = callInfo.callExpression.determinePosition(),
                                        )
                                    )
                                CallType.Container ->
                                    listOf(
                                        Discovered.Container(
                                            id = fullId,
                                            name = disambiguatedName,
                                            position = callInfo.callExpression.determinePosition(),
                                            tests =
                                            callInfo.callExpression
                                                .lambdaArguments
                                                .mapNotNull { it.getLambdaExpression() }
                                                .flatMap { it.bodyExpression?.findTests(fullId).orEmpty() }
                                                .toSet(),
                                        )
                                    )
                            }
                        }
                        .toSet()
                }
            }

    override fun discoverTests(
        expression: KtExpression?,
        classFqn: String,
    ): Set<Discovered> = expression?.findTests(classFqn).orEmpty()
}
