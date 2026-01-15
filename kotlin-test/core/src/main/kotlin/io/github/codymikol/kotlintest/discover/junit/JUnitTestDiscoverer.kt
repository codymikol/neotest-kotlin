package io.github.codymikol.kotlintest.discover.junit

import com.intellij.psi.util.childrenOfType
import io.github.codymikol.kotlintest.discover.TestDiscoverer
import io.github.codymikol.kotlintest.discover.model.Discovered
import io.github.codymikol.kotlintest.discover.model.DiscoveredResult
import io.github.codymikol.kotlintest.discover.model.determinePosition
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.kotlin.resolve.calls.util.getValueArgumentsInParentheses

/**
 * Supports JUnit 5 and 6 test discovery.
 *
 * [docs](https://docs.junit.org/6.0.1/overview.html)
 */
internal object JUnitTestDiscoverer : TestDiscoverer {
    private fun KtAnnotated.isDisabled(): Boolean =
        this.annotationEntries.any { annotation ->
            annotation.shortName?.identifier == "Disabled" ||
                annotation.shortName?.identifier == "Ignore"
        }

    private fun KtAnnotated.determineDisplayName(): String? =
        this
            .annotationEntries
            .firstOrNull { annotation -> annotation.shortName?.identifier == "DisplayName" }
            ?.getValueArgumentsInParentheses()
            ?.firstOrNull()
            ?.getArgumentExpression()
            ?.text
            ?.trim('"')

    private fun KtClass.discoverTests(parentId: String): Set<Discovered> =
        this.body
            ?.functions
            ?.filter { function ->
                !function.isPrivate() &&
                    function.annotationEntries.any { annotation ->
                        annotation.shortName?.identifier == "Test" ||
                            annotation.shortName?.identifier == "TestFactory" ||
                            annotation.shortName?.identifier == "ParameterizedTest"
                    } &&
                    !function.isDisabled()
            }?.mapNotNull { function ->
                val name = function.determineDisplayName() ?: function.name ?: return@mapNotNull null

                Discovered.Test(
                    id = "$parentId::$name",
                    name = name,
                    position = function.determinePosition(),
                )
            }?.toSet()
            .orEmpty() +
            this.body
                ?.childrenOfType<KtClass>()
                ?.filter { clazz ->
                    clazz.isInner() &&
                        clazz.annotationEntries.any { annotation -> annotation.shortName?.identifier == "Nested" } &&
                        !clazz.isDisabled()
                }?.mapNotNull { clazz ->
                    val name = clazz.determineDisplayName() ?: clazz.name ?: return@mapNotNull null
                    val fullId = "$parentId::$name"

                    Discovered.Container(
                        id = fullId,
                        name = name,
                        position = clazz.determinePosition(),
                        tests = clazz.discoverTests(fullId),
                    )
                }?.toSet()
                .orEmpty()

    override fun discoverTests(kotlinFile: KtFile): DiscoveredResult {
        val classes = kotlinFile.childrenOfType<KtClass>()

        return DiscoveredResult(
            tests = classes
                .filterNot { clazz -> clazz.isDisabled() }
                .mapNotNull { clazz ->
                    val classFqn = clazz.fqName?.asString() ?: return@mapNotNull null

                    Discovered.Container(
                        id = classFqn,
                        name = checkNotNull(clazz.determineDisplayName() ?: clazz.name),
                        position = clazz.determinePosition(),
                        tests = clazz.discoverTests(classFqn),
                    )
                }.toSet()
        )
    }
}
