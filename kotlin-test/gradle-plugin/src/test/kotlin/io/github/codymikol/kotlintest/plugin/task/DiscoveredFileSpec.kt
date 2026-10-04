package io.github.codymikol.kotlintest.plugin.task

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * The paths must match `discovered_path` of the Lua adapter (`lua/neotest-kotlin/test_files.lua`).
 */
class DiscoveredFileSpec : FunSpec({
    val directory = File("/root/build/kotlinTestFindTests/_app")

    withData(
        mapOf(
            "absolute path" to Pair("/root/app/src/test/kotlin/Spec.kt", "root/app/src/test/kotlin/Spec.kt.json"),
            "Windows path" to Pair("C:\\root\\Spec.kt", "C\\root\\Spec.kt.json"),
        ),
    ) { (path, expected) ->
        discoveredFile(directory, path) shouldBe directory.resolve(expected)
    }
})
