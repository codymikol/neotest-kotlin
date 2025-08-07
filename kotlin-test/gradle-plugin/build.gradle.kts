plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
    implementation(libs.bundles.jackson)
    implementation(libs.asm)
    compileOnly(libs.kotlin.gradle.plugin)

    testImplementation(libs.bundles.kotest)
}

gradlePlugin {
    plugins {
        create("kotlinTest") {
            id = "io.github.codymikol.kotlintest"
            implementationClass = "io.github.codymikol.kotlintest.plugin.KotlinTestPlugin"
        }
    }
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
