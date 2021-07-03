plugins {
    id("gimme-command")
}

dependencies {
    api(project(":gimme-command-core"))
    api(project(":gimme-command-discord"))
    api(project(":gimme-command-mc"))
}
