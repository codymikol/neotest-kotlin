import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    
    id("com.gradle.plugin-publish") version "1.2.1"
    id("com.gradleup.shadow") version "9.0.1"
}

/**
 * The test runner jar, embedded as a resource and put on the test runtime classpath of the project
 * by `kotlinTestExecute`. It is kept out of this plugin's own (shadowed) classes and dependencies, so
 * tests run with the project's versions of Kotest, JUnit and the Kotlin standard library.
 */
val runner: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

/**
 * Gradle TestKit tests applying the plugin to generated projects through the init script, see `src/functionalTest`.
 */
val functionalTest: SourceSet by sourceSets.creating

configurations[functionalTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[functionalTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    runner(project(":runner"))
    implementation(project(":core"))
    implementation(libs.asm)
    implementation(libs.bundles.jackson)
    compileOnly(libs.kotlin.gradle.plugin)

    testImplementation(libs.bundles.kotest)
}

gradlePlugin {
    // Puts the plugin under test metadata (its runtime classpath) and TestKit on the functional test classpath
    testSourceSets(functionalTest)

    website = "https://github.com/codymikol/neotest-kotlin"
    vcsUrl = "https://github.com/codymikol/neotest-kotlin.git"

    plugins {
        create("kotlinTest") {
            id = "io.github.codymikol.kotlintest"
            displayName = "kotlinTest"
            description = "Gradle plugin to execute and discover Kotlin tests for frameworks like JUnit and Kotest"
            implementationClass = "io.github.codymikol.kotlintest.plugin.KotlinTestPlugin"
            tags = listOf("test", "kotest", "JUnit", "kotlin")
        }
    }
}

/**
 * Gradle version the functional tests run builds with, e.g. `-PfunctionalTestGradleVersion=9.0.0`.
 * Defaults to the Gradle version running this build. CI runs the minimum supported and the latest version.
 */
val functionalTestGradleVersion: String =
    providers.gradleProperty("functionalTestGradleVersion").orNull?.takeIf { it.isNotBlank() } ?: gradle.gradleVersion

/**
 * The init script users apply the plugin with, the functional tests apply the plugin the same way.
 */
val initScript: File = rootDir.parentFile.resolve("test-logging.init.gradle.kts")

tasks.register<Test>("functionalTest") {
    description = "Runs Gradle TestKit tests of the plugin applied through the init script to generated projects."
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    shouldRunAfter(tasks.test)

    inputs.file(initScript).withPathSensitivity(PathSensitivity.NONE)
    systemProperty("kotlintest.functionalTest.gradleVersion", functionalTestGradleVersion)
    systemProperty("kotlintest.functionalTest.initScript", initScript.absolutePath)
}

detekt {
    source.from("src/functionalTest/kotlin")
}

tasks.processResources {
    from(runner) {
        into("io/github/codymikol/kotlintest/runner")
        rename { "kotlin-test-runner.jar" }
    }
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    mergeServiceFiles()
}

publishing {
    publications {
        create<MavenPublication>("kotlin-test") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = "kotlin-test"
            version = "1.0.0"
        }
    }

    repositories {
        mavenLocal()
    }
}
