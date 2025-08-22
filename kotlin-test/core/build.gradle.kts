plugins {
    `java-library`

    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

dependencies {
    implementation(libs.kotest.framework.engine)
    implementation(libs.bundles.junit)
    implementation(libs.coroutines)
    implementation(libs.reflect)

    compileOnly("org.jetbrains.kotlin:kotlin-compiler")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation(libs.bundles.jackson)

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.jackson)
    testImplementation("dev.zacsweers.kctfork:core:0.8.0")
    testImplementation(kotlin("test"))
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
