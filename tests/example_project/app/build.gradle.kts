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
    testImplementation(project(":testing"))
}

/*
 * Allows running the example project against other Kotest and JUnit versions than the version catalog's,
 * e.g. `-PkotestVersion=6.2.5 -PjunitVersion=6.1.3` or the ORG_GRADLE_PROJECT_kotestVersion environment variable.
 * Used by CI to cover the supported version range, see the "Compatibility" section of the README.
 */
val kotestVersion: String? = providers.gradleProperty("kotestVersion").orNull?.takeIf { it.isNotBlank() }
val junitVersion: String? = providers.gradleProperty("junitVersion").orNull?.takeIf { it.isNotBlank() }

// JUnit 5.x.y ships the JUnit Platform as 1.x.y, from JUnit 6 on both share a version
val junitPlatformVersion: String? = junitVersion?.let { if (it.startsWith("5.")) "1." + it.removePrefix("5.") else it }

configurations.configureEach {
    resolutionStrategy.eachDependency {
        when {
            kotestVersion != null && requested.group == "io.kotest" -> useVersion(kotestVersion)
            junitVersion != null && requested.group in setOf("org.junit.jupiter", "org.junit.vintage") ->
                useVersion(junitVersion)
            junitVersion != null && requested.group == "org.junit" && requested.name == "junit-bom" ->
                useVersion(junitVersion)
            junitPlatformVersion != null && requested.group == "org.junit.platform" -> useVersion(junitPlatformVersion)
        }
    }
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
