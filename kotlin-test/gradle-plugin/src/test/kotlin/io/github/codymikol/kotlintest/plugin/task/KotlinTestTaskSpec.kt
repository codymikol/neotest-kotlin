package io.github.codymikol.kotlintest.plugin.task

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlin.io.path.Path

class KotlinTestTaskSpec : FunSpec({
    context("Path.isTopLevelClass") {
        withData(
            mapOf(
                "Ends with .class" to Pair(Path("Example.class"), true),
                "Nested class" to Pair(Path($$"Example$Inner.class"), false),
                "Not Java class" to Pair(Path("RandomFile.json"), false),
            )
        ) { (input, expected) ->
            input.isTopLevelClass() shouldBe expected
        }
    }
})
