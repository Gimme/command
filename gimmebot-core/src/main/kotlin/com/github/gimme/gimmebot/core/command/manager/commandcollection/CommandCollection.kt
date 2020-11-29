package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * Represents a collection of commands.
 */
interface CommandCollection {
    /** Adds the given [command] to this collection. */
    fun addCommand(command: Command)

    /**
     * Returns the command from this collection that matches the given [input], or null if no command found.
     *
     * The input can include arguments, after the name of the command, that will be ignored.
     */
    fun getCommand(input: String): Command?

    /** Returns all commands in this collection. */
    fun getCommands(): List<Command>
}
