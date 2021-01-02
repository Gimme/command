package com.github.gimme.gimmebot.core.plugin

import com.github.gimme.gimmebot.core.GimmeBot
import com.github.gimme.gimmebot.core.command.manager.CommandManager
import com.github.gimme.gimmebot.core.common.Enableable

/**
 * Represents a plugin that can be installed in a [GimmeBot] to support a new platform.
 */
abstract class BasePlatformPlugin : Enableable {
    /** The bot that this plugin is installed in. */
    protected lateinit var bot: GimmeBot
        private set

    /** The plugin-specific command manager */
    abstract val commandManager: CommandManager<*>

    override var enabled: Boolean = false
        set(enabled) {
            field = Enableable.enable(this, enabled)
        }

    internal fun init(bot: GimmeBot) {
        this.bot = bot
    }
}
