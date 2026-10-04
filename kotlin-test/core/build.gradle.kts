plugins {
    `java-library`

    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.reflect)

    implementation(libs.kotlin.compiler)
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation(libs.bundles.jackson)

    implementation(libs.caffeine)
    // Required at runtime by the Analysis API's plugin descriptors, previously only provided transitively by Kotest
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.bundles.kotlin.analysis.api) {
        isTransitive = false
    }

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.jackson)
    testImplementation(kotlin("test"))
    testCompileOnly(libs.kotlin.compiler)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    explicitApi()

    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-parameters"))
    }
}

tasks.test {
    // Test sources of the runner are discovered by some tests, see RunnerFixtures.kt
    inputs.dir("../runner/src/test/kotlin")
        .withPropertyName("runnerFixtures")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
