plugins {
    id("gimme-command")
}

group = "dev.gimme.command"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    // API
    api(project(":gimme-command-core"))
    api("net.dv8tion:JDA:4.3.0_277")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = false
    }
}
