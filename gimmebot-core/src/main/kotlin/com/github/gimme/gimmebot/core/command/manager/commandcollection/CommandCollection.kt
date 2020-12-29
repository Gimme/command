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

    /** Returns the command that best matches the start of the given [path], or null if no match. */
    fun findCommand(path: List<String>): Command<*>?

    /** Returns if this collection contains the command with the specified [name]. */
    fun containsCommand(name: String): Boolean
}
