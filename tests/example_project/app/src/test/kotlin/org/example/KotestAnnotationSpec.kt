package org.example

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class KotestAnnotationSpec : AnnotationSpec() {
    @Test
    fun pass() {
        1 shouldBe 1
    }

    @Test
    fun fail() {
        1 shouldBe 2
    }

    @Test
    @Ignore
    fun ignore() {
        1 shouldBe 5
    }
}
