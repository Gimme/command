package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a command input medium with base functionality.
 *
 * @property commandPrefix prefix required for the input to be recognized as a command
 */
abstract class BaseCommandInputMedium(private val commandPrefix: String? = null) : CommandInputMedium {

    private lateinit var commandManager: CommandManager

    override fun install(commandManager: CommandManager) {
        this.commandManager = commandManager
        onInstall()
    }

    /** Performs logic when installed. */
    protected abstract fun onInstall()

    /** Sends the specified command [input] as the given [sender]. */
    protected fun send(sender: CommandSender, input: String) {
        var commandInput = input

        if (!commandPrefix.isNullOrEmpty()) {
            // Has to start with the command prefix
            if (!input.startsWith(commandPrefix)) return
            // Remove prefix
            commandInput = input.removePrefix(commandPrefix)
        }

        commandManager.parseInput(sender, commandInput)
    }
}
