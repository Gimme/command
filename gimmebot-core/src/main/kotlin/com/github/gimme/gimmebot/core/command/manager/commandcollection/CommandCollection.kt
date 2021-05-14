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
     * Returns the command from this collection with the [id], or null if no command with that [id] is registered.
     */
    fun getCommand(id: String): Command<*>?

    /** Returns if this collection contains the command with the [id]. */
    fun containsCommand(id: String): Boolean

    override fun iterator(): Iterator<Command<*>> = commands.iterator()
}
