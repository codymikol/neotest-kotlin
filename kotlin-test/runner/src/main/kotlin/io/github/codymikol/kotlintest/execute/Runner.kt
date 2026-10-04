package io.github.codymikol.kotlintest.execute

/**
 * Entry point of the test runner, which executes tests inside the test runtime classpath of the
 * project, writing a JSON [RunReport] to `--output`.
 *
 * This is deliberately free of dependencies other than the Kotlin standard library and the test
 * frameworks already on the project's classpath, see [TestFrameworkExecutor].
 */
public fun main(args: Array<String>) {
    val options = RunnerOptions.parse(args)
    val classLoader = TestFrameworkExecutor::class.java.classLoader
    val classes = options.classes.map { className -> Class.forName(className, false, classLoader).kotlin }.toSet()

    val report = TestFrameworkExecutor.runAll(classes = classes, filter = options.filter)

    options.output.parentFile?.mkdirs()
    options.output.writeText(report.toJson())
}
