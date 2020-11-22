import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    `java-library`
    `maven-publish`
}
group = "com.github.gimme.gimmebot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}
dependencies {
    api(project(":gimmebot-core"))
    implementation("net.dv8tion:JDA:4.2.0_217")
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "13"
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
