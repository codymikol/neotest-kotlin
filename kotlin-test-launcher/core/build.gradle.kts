plugins {
    alias(libs.plugins.kotlinx.serialization)
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

kotlin {
    explicitApi()
}
