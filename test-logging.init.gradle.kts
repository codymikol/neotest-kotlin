/**
 * Gradle Init Script for neotest-kotlin
 *
 * This helps force standardized and usable output for tests
 * so that the plugin can effectively parse the output and it's usable
 * for users.
 */
import io.github.codymikol.kotlintestlauncher.plugin.KotlinTestLauncherPlugin

initscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        classpath("io.github.codymikol:kotlin-test-launcher:1.0.0")
    }
}

allprojects {
    afterEvaluate {
        apply<KotlinTestLauncherPlugin>()
    }
}
