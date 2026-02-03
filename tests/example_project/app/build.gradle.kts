plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.dependency.management)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.spring)

    testImplementation(libs.bundles.spring.test)
    testImplementation(libs.bundles.kotest)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
