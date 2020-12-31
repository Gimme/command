package com.github.gimme.gimmebot.spigot

import com.github.gimme.gimmebot.core.command.medium.TextCommandMedium
import org.bukkit.plugin.java.JavaPlugin

/**
 * Handles input/output through Minecraft chat.
 */
class SpigotCommandMedium(
    private val plugin: JavaPlugin,
    includeHelpCommand: Boolean = true,
    includeConsoleListener: Boolean = true,
) : TextCommandMedium(includeHelpCommand, includeConsoleListener, "/") {

    override fun onInstall() {
        super.onInstall()

        TODO("Start listening to command input from MC chat")
    }
}
