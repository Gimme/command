plugins {
    id("command-core")
}

dependencies {
    // Kotlin
    implementation(kotlin("reflect"))

    // Test
    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("com.github.spotbugs:spotbugs-annotations:4.2.3")

    // Other
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
}
