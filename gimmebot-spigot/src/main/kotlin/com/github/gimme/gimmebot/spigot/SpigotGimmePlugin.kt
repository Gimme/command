package com.github.gimme.gimmebot.spigot

import org.bukkit.plugin.java.JavaPlugin

/**
 * A Spigot plugin that utilizes a [com.github.gimme.gimmebot.core.GimmeBot] to do the work.
 */
abstract class SpigotGimmePlugin : JavaPlugin() {

    private lateinit var spigotGimmeBot: SpigotGimmeBot

    /** Creates a new bot, ready to be started and do some work. */
    protected abstract fun createBot(): SpigotGimmeBot

    /** Starts the bot to do its work. */
    override fun onEnable() {
        initializeBot()

        spigotGimmeBot.plugin = this
        spigotGimmeBot.start()
    }

    /** Stops the bot. */
    override fun onDisable() {
        spigotGimmeBot.stop()
    }

    private fun initializeBot() {
        if (!this::spigotGimmeBot.isInitialized) spigotGimmeBot = createBot()
    }
}
