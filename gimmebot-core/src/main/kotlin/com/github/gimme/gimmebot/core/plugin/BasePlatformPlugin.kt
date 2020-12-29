package com.github.gimme.gimmebot.core.plugin

import com.github.gimme.gimmebot.core.GimmeBot
import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a plugin that can be installed in a [GimmeBot] to support a new platform.
 */
abstract class BasePlatformPlugin {
    /** The bot that this plugin is installed in. */
    protected lateinit var bot: GimmeBot
        private set

    /** The plugin-specific command manager */
    abstract val commandManager: CommandManager<*>

    /** If this plugin is enabled. */
    var enabled: Boolean = false
        set(enabled) {
            if (field == enabled) return
            field = enabled

            if (enabled) onEnable() else onDisable()
        }

    internal fun init(bot: GimmeBot) {
        this.bot = bot
    }

    /** Performs logic when enabled. */
    abstract fun onEnable()

    /** Performs logic when disabled. */
    open fun onDisable() {}
}
