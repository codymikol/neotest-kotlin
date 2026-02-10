import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    
    id("com.gradle.plugin-publish") version "1.2.1"
    id("com.gradleup.shadow") version "9.0.1"
}

dependencies {
    implementation(project(":core"))
    implementation(libs.asm)
    implementation(libs.bundles.jackson)
    compileOnly(libs.kotlin.gradle.plugin)

    testImplementation(libs.bundles.kotest)
}

gradlePlugin {
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
