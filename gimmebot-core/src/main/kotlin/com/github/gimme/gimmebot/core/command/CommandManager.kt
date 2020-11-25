package com.github.gimme.gimmebot.core.command

/**
 * Represents a command manager that handles the registration and execution of commands.
 */
interface CommandManager {

    /** Registers the given [command] the be executable by this command manager. */
    fun registerCommand(command: Command)

    /** Returns all registered commands. */
    fun getCommands(): Collection<Command>

    /** Returns the command with the specified [name] if it has been registered. */
    fun getCommand(name: String): Command?

    /**
     * Checks the given [input] if a valid command and then executes it as the given [commandSender]. Returns if a valid
     * command.
     */
    fun parseInput(commandSender: CommandSender, input: String): Boolean
}
