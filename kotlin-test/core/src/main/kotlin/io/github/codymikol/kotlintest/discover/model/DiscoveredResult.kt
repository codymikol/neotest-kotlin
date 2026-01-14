package io.github.codymikol.kotlintest.discover.model

public data class DiscoveredResult(
    val tests: Set<Discovered.Container> = emptySet(),
    val warnings: List<TestWarning> = emptyList()
)
