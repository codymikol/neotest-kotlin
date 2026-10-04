/*
 * Shared test support code (e.g. base specs) consumed by other projects,
 * like an internal testing library would be.
 */
plugins {
    alias(libs.plugins.jvm)
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kotest.runner.junit5)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
