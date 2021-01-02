package com.github.gimme.gimmebot.core.plugin

import com.github.gimme.gimmebot.core.command.manager.CommandManager
import com.github.gimme.gimmebot.core.command.manager.TextCommandManager
import com.github.gimme.gimmebot.core.command.medium.CommandMedium

/**
 * Represents a text-based platform plugin.
 *
 * @property commandMedium the command medium that handles input/output on this platform
 */
abstract class TextPlatformPlugin : BasePlatformPlugin() {

    lateinit var commandMedium: CommandMedium<String?>

    private val _commandManager = TextCommandManager()
    override val commandManager: CommandManager<String?>
        get() = _commandManager

    override fun onEnable() {
        this.commandMedium = initCommandMedium().apply {
            registerCommandManager(bot.commandManager) { it?.toString() }
            registerCommandManager(commandManager) { it }
            enable()
        }
    }

    override fun onDisable() {
        commandMedium.disable()
    }

    /**
     * Initializes and returns the command medium to be installed with this plugin.
     */
    protected abstract fun initCommandMedium(): CommandMedium<String?>
}
