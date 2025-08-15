plugins {
    `java-library`

    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

dependencies {
    implementation(libs.kotest.framework.engine)
    implementation(libs.bundles.junit)
    implementation(libs.coroutines)
    implementation(libs.reflect)

    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation(libs.bundles.jackson)

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.jackson)
    testImplementation(kotlin("test"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    explicitApi()
}
