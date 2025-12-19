plugins {
    `java-library`

    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

dependencies {
    implementation(libs.kotest.framework.engine)
    implementation(libs.bundles.junit)
    implementation(libs.coroutines)
    implementation(libs.reflect)

    implementation(libs.kotlin.compiler)
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation(libs.bundles.jackson)

    implementation(libs.caffeine)
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
