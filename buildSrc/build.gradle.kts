plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    val kotlinVersion = "1.5.20"

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32")
}
