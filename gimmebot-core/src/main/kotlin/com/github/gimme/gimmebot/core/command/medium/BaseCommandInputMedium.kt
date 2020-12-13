package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a command input medium with base functionality.
 *
 * @property commandPrefix prefix required for the input to be recognized as a command
 */
abstract class BaseCommandInputMedium : CommandInputMedium {

    protected abstract val commandPrefix: String?
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
        val prefix = commandPrefix

        if (!prefix.isNullOrEmpty()) {
            // Has to start with the command prefix
            if (!input.startsWith(prefix)) return
            // Remove prefix
            commandInput = input.removePrefix(prefix)
        }

        commandManager.parseInput(sender, commandInput)
    }
}
