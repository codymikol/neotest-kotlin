package io.github.codymikol.kotlintest

import java.io.File

/**
 * Test sources of the runner module, whose execution results are asserted there.
 *
 * Discovery of these is asserted here, so the ids produced by discovery are tied to the ids
 * produced when executing the very same source, without depending on the runner module.
 */
internal fun runnerFixture(path: String): File =
    File("../runner/src/test/kotlin/io/github/codymikol/kotlintest", path).also {
        check(it.isFile) { "Runner fixture $it not found" }
    }
