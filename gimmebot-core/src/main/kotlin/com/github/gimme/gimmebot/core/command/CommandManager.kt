package com.github.gimme.gimmebot.core.command

/**
 * Represents a command manager that handles the registration and execution of commands.
 */
interface CommandManager {

    /** Registers the given [command] the be executable by this command manager. */
    fun registerCommand(command: Command)

    /** Returns the command with the specified [name] if it has been registered. */
    fun getCommand(name: String): Command?

    /** Executes the given [command] as the given [commandSender]. */
    fun executeCommand(commandSender: CommandSender, command: Command)
}
