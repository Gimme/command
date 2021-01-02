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
        installCommandMedium()
    }

    private fun installCommandMedium() {
        this.commandMedium = initCommandMedium().also { medium ->
            medium.registerCommandManager(bot.commandManager) { it?.toString() }
            medium.registerCommandManager(commandManager) { it }
            medium.enable()
        }
    }

    /**
     * Initializes and returns the command medium to be installed with this plugin.
     */
    protected abstract fun initCommandMedium(): CommandMedium<String?>
}
