plugins {
    id("command-core")
}

dependencies {
    api(project(":command-core"))
    api(project(":command-discord"))
    api(project(":command-mc"))
}
