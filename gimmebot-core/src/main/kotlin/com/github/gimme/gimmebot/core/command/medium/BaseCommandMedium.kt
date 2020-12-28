package com.github.gimme.gimmebot.core.command.medium

import com.github.gimme.gimmebot.core.command.CommandException
import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.ConsoleCommandSender
import com.github.gimme.gimmebot.core.command.MessageReceiver
import com.github.gimme.gimmebot.core.command.manager.CommandManager

/**
 * Represents a command input medium with base functionality.
 *
 * @param T the command manager's response type
 * @param R the output response type
 * @property responseWrapper a wrapper to convert the command response
 */
abstract class BaseCommandMedium<T, R>(
    override var commandManager: CommandManager<T>,
    var responseWrapper: (T) -> R,
    includeConsoleListener: Boolean = false,
) : CommandMedium<T, R> {

    private val ioListeners: MutableList<MessageReceiver> = mutableListOf()

    init {
        if (includeConsoleListener) {
            addIOListener { ConsoleCommandSender }
        }
    }

    override fun parseInput(sender: CommandSender, input: String) {
        ioListeners.forEach { it.sendMessage("${sender.name}: $input") }
    }

    override fun respond(commandSender: CommandSender, response: R) {
        ioListeners.forEach { it.sendMessage(response.toString()) }
    }

    /**
     * Executes a command through the [commandManager] and wraps the response with this medium's [responseWrapper].
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    protected fun executeCommand(commandSender: CommandSender, commandName: String, arguments: List<String>): R {
        return responseWrapper(commandManager.executeCommand(commandSender, commandName, arguments))
    }

    final override fun addIOListener(messageReceiver: MessageReceiver) {
        ioListeners.add(messageReceiver)
    }

    override fun install() {
        onInstall()
    }

    /** Performs logic when installed. */
    protected abstract fun onInstall()
}
