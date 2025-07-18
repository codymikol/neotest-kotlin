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

    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "io.github.codymikol.neotestkotlin.AppKt"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
