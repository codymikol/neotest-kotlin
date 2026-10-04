/*
 * A second project with its own tests, in the same package as the tests of `app` and with a spec of the
 * same name (`org.example.SharedNameSpec`), to cover discovery and execution of multi-project builds.
 *
 * Unlike `app`, it always uses the Kotest and JUnit versions of the version catalog, so projects of the
 * build may use different versions (e.g. when CI overrides the versions of `app`).
 */
plugins {
    alias(libs.plugins.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotest and a base spec from another project
    testImplementation(project(":testing"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
