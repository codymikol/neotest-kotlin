plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
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
