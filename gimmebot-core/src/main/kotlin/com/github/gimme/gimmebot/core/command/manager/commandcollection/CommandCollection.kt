package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * Represents a collection of commands.
 */
interface CommandCollection {

    /** Returns all commands in this collection. */
    val commands: List<Command<*>>

    /** Adds the given [command] to this collection. */
    fun addCommand(command: Command<*>)

    /**
     * Returns the command from this collection with the [name], or null if no command with that [name] is registered.
     */
    fun getCommand(name: String): Command<*>?

    /** Returns the command that best matches the start of the [path], or null if no match. */
    fun findCommand(path: List<String>): Command<*>?

    /** Returns if this collection contains the command with the [name]. */
    fun containsCommand(name: String): Boolean
}
