plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
    implementation(libs.bundles.jackson)

    testImplementation(libs.bundles.kotest)
}

gradlePlugin {
    plugins {
        create("kotlinTestLauncher") {
            id = "io.github.codymikol.kotlintestlauncher"
            implementationClass = "io.github.codymikol.kotlintestlauncher.plugin.KotlinTestLauncherPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("kotlin-test-launcher") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = "kotlin-test-launcher"
            version = "1.0.0"
        }
    }

    repositories {
        mavenLocal()
    }
}
