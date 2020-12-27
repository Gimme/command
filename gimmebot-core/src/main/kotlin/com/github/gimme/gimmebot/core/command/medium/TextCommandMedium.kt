package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.CommandException
import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.ErrorCode
import com.github.gimme.gimmebot.core.command.MessageReceiver
import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a text-based command medium with, for example a chat box or a command line interface.
 *
 * @property commandPrefix prefix required for the input to be recognized as a command
 */
abstract class TextCommandMedium(commandManager: CommandManager<String?>) : BaseCommandMedium<String?>(commandManager) {

    protected abstract val commandPrefix: String?
    private val ioListeners: MutableList<MessageReceiver> = mutableListOf()

    init {
        addIOListener { message -> println(message) }
    }

    final override fun addIOListener(messageReceiver: MessageReceiver) {
        ioListeners.add(messageReceiver)
    }

    /** Sends the specified command [input] as the given [sender]. */
    protected fun parseInput(sender: CommandSender, input: String) {
        val message = send(sender, input)

        // Send back the response
        respond(sender, message)
    }

    private fun send(sender: CommandSender, input: String): String? {
        val commandInput = validatePrefix(input) ?: return null

        ioListeners.forEach { it.sendMessage("${sender.name}: $input") }

        return try { // Execute the command
            executeCommand(sender, commandInput)
        } catch (e: CommandException) { // The command returned with an error
            e.message
        }
    }

    @Throws(CommandException::class)
    private fun executeCommand(commandSender: CommandSender, input: String): String? {
        val command = commandManager.commandCollection.findCommand(input.split(" ")) ?: throw ErrorCode.NOT_A_COMMAND.createException()

        // Remove command name, leaving only the arguments
        val argsInput = input.removePrefix(command.name)

        // Split into words on spaces, ignoring spaces between two quotation marks
        val args = argsInput.split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*\$)".toRegex())
            .map { s -> s.replace("\"", "") }.drop(1)

        return commandManager.executeCommand(commandSender, command.name, args)
    }

    private fun respond(sender: CommandSender, message: String?) {
        if (message.isNullOrEmpty()) return

        sender.sendMessage(message)
        ioListeners.forEach { it.sendMessage(message) }
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
