/**
 * Gradle Init Script for neotest-kotlin
 *
 * This helps force standardized and usable output for tests
 * so that the plugin can effectively parse the output and it's usable
 * for users.
 */
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

allprojects {
    afterEvaluate {
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

    tasks.register("printProjectPaths") {
        group = "help"
        description = "Prints all project names and their absolute paths in a parseable format"
        doLast {
            allprojects.forEach { p ->
                println("NEOTEST_GRADLE_PROJECT\t${p.path}\t${p.projectDir.absolutePath}")
            }
        }
    }
}
