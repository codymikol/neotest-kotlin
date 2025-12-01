package io.github.codymikol.kotlintest

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import io.github.codymikol.kotlintest.command.Discover
import io.github.codymikol.kotlintest.command.Execute
import io.github.codymikol.kotlintest.command.KotlinTest

public fun main(args: Array<String>): Unit =
    KotlinTest()
        .subcommands(Execute(), Discover())
        .main(args)
