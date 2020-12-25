package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * Represents a collection of commands.
 */
interface CommandCollection {

    /** Adds the given [command] to this collection. */
    fun addCommand(command: Command<*>)

    /**
     * Returns the command from this collection with the specified [name], or null if no command with that name is
     * registered.
     */
    fun getCommand(name: String): Command<*>?

    /** Returns all commands in this collection. */
    fun getCommands(): List<Command<*>>
}
