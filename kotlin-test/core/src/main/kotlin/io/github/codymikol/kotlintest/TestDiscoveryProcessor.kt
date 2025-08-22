package io.github.codymikol.kotlintest

import io.kotest.common.reflection.bestName
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.processAllDeclarations
import org.jetbrains.kotlin.fir.declarations.utils.superConeTypes
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.types.ConstantValueKind

/**
 * The entry point for Kotlin Test Discovery.
 */
public class TestDiscoveryExtension(
    session: FirSession,
) : FirAdditionalCheckersExtension(session) {
    override val declarationCheckers: DeclarationCheckers =
        object : DeclarationCheckers() {
            override val classCheckers: Set<FirClassChecker>
                get() = super.classCheckers + KotestClassChecker(MppCheckerKind.Common)
        }
}

public data class DiscoveredTest(
    val id: String,
    val filename: String,
    /**
     * Line number position in the file.
     */
    val position: Position,
    val type: TestType,
)

public enum class TestType {
    TEST,
    CONTAINER,
}

public data class Position(
    /**
     * Starting line number (starting with 1)
     */
    val start: Int,
    /**
     * Ending line number (starting with 1).
     */
    val end: Int,
)

/**
 * Determines the start and end line numbers for the [element] in the [KtSourceFile].
 *
 * @throws IllegalArgumentException [KtSourceElement.endOffset] is greater than the [KtSourceFile.getContentsAsStream] length
 */
internal fun KtSourceFile.getElementPosition(element: KtSourceElement): Position {
    val newlineByte = '\n'.code.toByte()
    val contentUpTillElementEnd =
        this.getContentsAsStream().use { stream ->
            stream.readNBytes(element.endOffset).also {
                require(it.size == element.endOffset) {
                    "Element supposedly ends at ${element.endOffset}, but only ${it.size} bytes exist in '${this.name}'"
                }
            }
        }

    val start = contentUpTillElementEnd.take(element.startOffset).count { it == newlineByte } + 1

    return Position(
        start = start,
        end =
            start +
                contentUpTillElementEnd
                    .takeLast(element.endOffset - element.startOffset)
                    .count { it == newlineByte },
    )
}

public class KotestClassChecker(
    mppKind: MppCheckerKind,
) : FirDeclarationChecker<FirClass>(mppKind) {
    public companion object {
        public val SUPPORTED_EXTENSION_NAMES: Set<String> = setOf("test", "context")
        public val SUPPORTED_RECEIVER_NAMES: Set<String> = setOf(FunSpec::class.bestName(), FunSpecContainerScope::class.bestName())
    }

    private fun ClassId?.isFunSpec() = this?.asFqNameString() == FunSpec::class.bestName()

    /**
     * Determines if this [FirFunctionCall] is a valid Kotest "test" or "context" in the scope of FunSpec.
     */
    private fun FirFunctionCall.isFunSpecTestOrContext(): Boolean {
        val receiverFullyQualifiedClassName =
            this.dispatchReceiver
                ?.resolvedType
                ?.classId
                ?.asFqNameString()
        if (this.calleeReference.name.asString() !in SUPPORTED_EXTENSION_NAMES ||
            receiverFullyQualifiedClassName !in SUPPORTED_RECEIVER_NAMES ||
            this.arguments.size != 2
        ) {
            return false
        }

        val name = this.arguments.first() as? FirLiteralExpression ?: return false
        val anonymousFunction = this.arguments.last() as? FirAnonymousFunctionExpression ?: return false

        return name.kind == ConstantValueKind.String && anonymousFunction.isTrailingLambda
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirClass) {
        val file = context.containingFile?.sourceFile
        if (declaration.superConeTypes.none { it.classId?.isFunSpec() == true } || file == null) {
            return
        }

        val discoveredTests: MutableList<DiscoveredTest> = mutableListOf()

        /**
         * Recursive depth first search for FunSpec "test" and "context" tests.
         * [id] being null should only be passed for top-level tests/containers.
         */
        fun FirAnonymousFunction.processFunSpecTests(id: String? = null) {
            this.body
                ?.statements
                ?.filterIsInstance<FirFunctionCall>()
                ?.filter { it.isFunSpecTestOrContext() }
                ?.forEach {
                    val source = checkNotNull(it.source)
                    val testId =
                        listOfNotNull(id, (it.arguments.first() as FirLiteralExpression).value.toString()).joinToString(
                            separator = "::",
                        )

                    when (it.calleeReference.name.asString()) {
                        "test" -> {
                            discoveredTests.add(
                                DiscoveredTest(
                                    id = testId,
                                    filename = checkNotNull(file.path),
                                    position = file.getElementPosition(source),
                                    type = TestType.TEST,
                                ),
                            )
                        }
                        "context" -> {
                            discoveredTests.add(
                                DiscoveredTest(
                                    id = testId,
                                    filename = checkNotNull(file.path),
                                    position = file.getElementPosition(source),
                                    type = TestType.CONTAINER,
                                ),
                            )

                            val anonymousFunctionExpression = it.arguments[1] as FirAnonymousFunctionExpression
                            anonymousFunctionExpression.anonymousFunction.processFunSpecTests(testId)
                        }
                    }
                }
        }

        declaration.processAllDeclarations(context.session) { symbol ->
            if (symbol !is FirConstructorSymbol) {
                return@processAllDeclarations
            }

            val constructor = symbol.resolvedDelegatedConstructorCall ?: return@processAllDeclarations
            val constructorBody =
                constructor.arguments
                    .filterIsInstance<FirAnonymousFunctionExpression>()
                    .firstOrNull() ?: return@processAllDeclarations

            constructorBody.anonymousFunction.processFunSpecTests()
        }

        // TODO: Unsure of how to assert on output in tests, so `println`ing for now just to do manual validations.
        println(discoveredTests)
    }
}
