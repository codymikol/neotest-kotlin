package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringExtension

object ProjectConfig : AbstractProjectConfig() {
    override val extensions: List<Extension> = listOf(SpringExtension())
}
