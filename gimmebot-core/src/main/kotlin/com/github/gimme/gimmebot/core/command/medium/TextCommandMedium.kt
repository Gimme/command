package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.CommandException
import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.ErrorCode
import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a text-based command medium with, for example a chat box or a command line interface.
 *
 * @property commandPrefix prefix required for the input to be recognized as a command
 */
abstract class TextCommandMedium(commandManager: CommandManager<String?>) : BaseCommandMedium<String?>(commandManager) {

    protected abstract val commandPrefix: String?

    override fun parseInput(sender: CommandSender, input: String) {
        val commandInput = validatePrefix(input) ?: return

        super.parseInput(sender, input)

        val message = try { // Execute the command
            executeCommand(sender, commandInput)
        } catch (e: CommandException) { // The command returned with an error
            e.message
        }

        // Send back the response
        respond(sender, message)
    }

    override fun respond(commandSender: CommandSender, response: String?) {
        if (response.isNullOrEmpty()) return

        super.respond(commandSender, response)

        commandSender.sendMessage(response)
    }

    @Throws(CommandException::class)
    private fun executeCommand(commandSender: CommandSender, commandInput: String): String? {
        val command = commandManager.commandCollection.findCommand(commandInput.split(" "))
            ?: throw ErrorCode.NOT_A_COMMAND.createException()

        // Remove command name, leaving only the arguments
        val argsInput = commandInput.removePrefix(command.name)

        // Split into words on spaces, ignoring spaces between two quotation marks
        val args = argsInput.split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
            .map { s -> s.replace("\"", "") }.drop(1)

        return commandManager.executeCommand(commandSender, command.name, args)
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
