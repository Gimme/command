package com.github.gimme.gimmebot.spigot

import com.github.gimme.gimmebot.core.command.medium.CommandMedium
import com.github.gimme.gimmebot.core.plugin.TextPlatformPlugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * A plugin that connects the bot to a Spigot plugin.
 *
 * @property plugin the Spigot plugin
 */
open class SpigotPlatformPlugin(protected val plugin: JavaPlugin) : TextPlatformPlugin() {

    override fun initCommandMedium(): CommandMedium<String?> {
        return SpigotCommandMedium(plugin)
    }
}
