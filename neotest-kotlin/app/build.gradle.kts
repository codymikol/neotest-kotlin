import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotest.framework.engine)
    implementation(libs.coroutines)
    implementation(libs.json)
    implementation(libs.reflect)

    testImplementation(libs.bundles.kotest)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "io.github.codymikol.neotestkotlin.AppKt"
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
