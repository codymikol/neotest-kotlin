import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
}

allprojects {
    group = "io.github.codymikol"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()

        // Only run tests that end with Spec
        include("**/*Spec.class")

        testLogging {
            showStandardStreams = true
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}
