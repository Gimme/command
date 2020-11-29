package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.CommandSender
import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection

/**
 * Represents a command manager that handles the registration and execution of commands.
 */
interface CommandManager {

    /** Registers the given [command] the be executable by this command manager. */
    fun registerCommand(command: Command)

    /** Returns the command with the specified [name] if it has been registered. */
    fun getCommand(name: String): Command?

    /** Collection of all registered commands. */
    fun getCommandCollection(): CommandCollection

    /**
     * Checks the given [input] if a valid command and then executes it as the given [commandSender]. Returns if a valid
     * command and it was successfully executed.
     */
    fun parseInput(commandSender: CommandSender, input: String): Boolean
}
