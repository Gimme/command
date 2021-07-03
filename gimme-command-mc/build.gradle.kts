plugins {
    id("gimme-command")
}

group = "dev.gimme.command"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/groups/public")
}

dependencies {
    // API
    api(project(":gimme-command-core"))

    // Spigot
    compileOnly("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT")
}
