package io.github.codymikol.kotlintest.plugin

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

/**
 * The names must match `project.file_name` of the Lua adapter (`lua/neotest-kotlin/project.lua`).
 */
class ProjectFileNameSpec : FunSpec({
    withData(
        mapOf(
            "root project" to Pair(":", "_.json"),
            "subproject" to Pair(":app", "_app.json"),
            "nested subproject" to Pair(":app:core", "_app_core.json"),
        ),
    ) { (projectPath, expected) ->
        projectFileName(projectPath) shouldBe expected
    }
})
