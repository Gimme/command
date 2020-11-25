package com.github.gimme.gimmebot.spigot

import com.github.gimme.gimmebot.core.GimmeBot
import org.bukkit.plugin.java.JavaPlugin

/**
 * Represents a bot that starts through a Spigot plugin.
 */
abstract class SpigotGimmeBot : GimmeBot() {

    /** The plugin that this bot started through. */
    lateinit var plugin: JavaPlugin internal set

    //TODO: Not yet implemented (e.g., connect command system)
}
