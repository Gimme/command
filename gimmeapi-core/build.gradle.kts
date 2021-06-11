plugins {
    kotlin("jvm") version "1.5.0"
    `java-library`
    `maven-publish`
    id("org.jetbrains.dokka") version "1.4.32"
}

group = "dev.gimme.gimmeapi.core"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Logging
    implementation("io.github.microutils:kotlin-logging:2.0.3")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    // Test
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("org.mockito:mockito-core:3.6.28")
    testImplementation("org.mockito:mockito-inline:3.6.28")

    // Other
    implementation("org.yaml:snakeyaml:1.27")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "14"
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-progressive")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
            val javadocJar by tasks.creating(Jar::class) {
                dependsOn(dokkaHtml)
                archiveClassifier.set("javadoc")
                from(dokkaHtml.outputDirectory)
            }
            artifact(javadocJar)

            from(components["java"])
        }
    }
}
