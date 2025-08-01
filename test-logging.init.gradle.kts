/**
 * Gradle Init Script for neotest-kotlin
 *
 * This helps force standardized and usable output for tests
 * so that the plugin can effectively parse the output and it's usable
 * for users.
 */
import io.github.codymikol.kotlintestlauncher.plugin.KotlinTestLauncherPlugin
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

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

        tasks.withType<Test>().configureEach {
            /**
             * Force re-run the tests so we have output to parse
             * [docs](https://blog.gradle.org/stop-rerunning-tests)
             */
            outputs.upToDateWhen { false }

            testLogging {
                events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }
}
