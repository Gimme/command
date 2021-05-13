package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * Represents a collection of commands.
 */
interface CommandCollection : Iterable<Command<*>> {

    /** Returns all commands in this collection. */
    val commands: Set<Command<*>>

    /** Adds the given [command] to this collection. */
    fun addCommand(command: Command<*>)

    /**
     * Returns the command from this collection with the [name], or null if no command with that [name] is registered.
     */
    fun getCommand(name: String): Command<*>?

    /** Returns if this collection contains the command with the [name]. */
    fun containsCommand(name: String): Boolean

    override fun iterator(): Iterator<Command<*>> = commands.iterator()
}
