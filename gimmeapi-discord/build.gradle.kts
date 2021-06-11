import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    `java-library`
    `maven-publish`
    id("org.jetbrains.dokka") version "1.4.32"
}

group = "dev.gimme.gimmeapi.discord"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    // Project
    api(project(":gimmeapi-core"))

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))

    // Test
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Other
    api("net.dv8tion:JDA:4.3.0_277")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "14"
        freeCompilerArgs = listOf("-progressive")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_13
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
