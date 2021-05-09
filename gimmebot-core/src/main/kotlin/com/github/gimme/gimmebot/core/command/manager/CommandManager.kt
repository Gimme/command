package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection
import com.github.gimme.gimmebot.core.command.sender.CommandSender
import com.github.gimme.gimmebot.core.common.grouped.Grouped

/**
 * Represents a command manager that handles the registration and execution of commands.
 *
 * @param R the command execution response type
 */
interface CommandManager<R> {

    /** The mutable collection of all registered commands. */
    val commandCollection: CommandCollection

    /**
     * Registers the given [command] to be executable through this manager with the specified [responseConverter] to
     * convert the result of the command execution from [T] to the uniform type [R].
     */
    fun <T> registerCommand(command: Command<T>, responseConverter: ((T) -> R)? = null)

    /** Returns the command with the specified [id] if it has been registered, else null. */
    fun getCommand(id: Grouped): Command<*>?

    /** Returns if the command with the specified [id] has been registered. */
    fun hasCommand(id: Grouped): Boolean

    /**
     * Executes the registered command with the specified [commandId] with the specified [arguments] as the given
     * [commandSender] and returns the response.
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    fun executeCommand(commandSender: CommandSender, commandId: Grouped, arguments: List<String> = listOf()): R
}
