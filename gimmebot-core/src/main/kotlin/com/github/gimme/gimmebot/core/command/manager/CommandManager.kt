package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandTree

/**
 * Represents a command manager that handles the registration and execution of commands.
 */
interface CommandManager {

    /** The mutable collection of all registered commands. */
    val commandCollection: CommandTree

    /** Registers the given [command] to be executable by this command manager. */
    fun registerCommand(command: Command<*>)

    /** Returns the command with the specified [name] if it has been registered. */
    fun getCommand(name: String): Command<*>?

    /**
     * Checks the given [commandName] and [arguments] if a valid command call and then executes it as the given
     * [commandSender] and returns the response.
     */
    fun executeCommand(commandSender: CommandSender, commandName: String, arguments: List<String> = listOf()): Any?
}
