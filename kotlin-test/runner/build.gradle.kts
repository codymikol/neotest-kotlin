import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

/*
 * The test runner executed inside the test runtime classpath of the project under test.
 *
 * It must not bring along any library that could conflict with the project's own dependencies,
 * so the test frameworks and kotlinx-coroutines are compileOnly and provided by the project at
 * runtime. It targets an older Kotlin API and JVM so it can run with the project's
 * kotlin-stdlib and JDK. See the "Compatibility" section of the README.
 */
plugins {
    `java-library`
}

dependencies {
    compileOnly(libs.kotest.framework.engine)
    compileOnly(libs.junit.launcher)
    compileOnly(libs.coroutines)

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.coroutines)
    testImplementation(kotlin("test"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

kotlin {
    explicitApi()

    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        freeCompilerArgs.add("-Xjdk-release=11")
        // Kotest 6.1 requires kotlin-stdlib 2.2, but keep the runner usable with older stdlib versions
        apiVersion = KotlinVersion.KOTLIN_2_1
        languageVersion = KotlinVersion.KOTLIN_2_1
    }
}

