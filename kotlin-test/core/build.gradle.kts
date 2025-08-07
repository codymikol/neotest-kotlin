plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    implementation(libs.kotest.framework.engine)
    implementation(libs.coroutines)
    implementation(libs.reflect)

    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.jackson)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    explicitApi()
}

publishing {
    publications {
        create<MavenPublication>("kotlin-test-core") {
            groupId = project.group.toString()
            artifactId = "kotlin-test-core"
            version = "1.0.0"
            from(components["java"])
        }
    }
}
