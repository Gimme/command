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
        var commandInput = validatePrefix(input) ?: return

        //TODO outputListeners.forEach { it.sendMessage("${commandSender.name}: $input") }

        // Return if not a valid command
        val command = commandManager.getCommand(commandInput) ?: return // TODO: should return error message
        // Remove command name, leaving only the arguments
        commandInput = commandInput.removePrefix(command.name)

        // Split into words on spaces, ignoring spaces between two quotation marks
        val args = commandInput.split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
            .map { s -> s.replace("\"", "") }.drop(1)

        commandManager.parseInput(sender, command.name, args)
    }

    /**
     * If the given input starts with the [commandPrefix], returns a copy of the input with the prefix removed.
     * Otherwise, returns null.
     */
    private fun validatePrefix(input: String): String? {
        val prefix = commandPrefix

        if (prefix.isNullOrEmpty()) return input

        // Has to start with the command prefix
        if (!input.startsWith(prefix)) return null
        // Remove prefix
        return input.removePrefix(prefix)
    }
}
