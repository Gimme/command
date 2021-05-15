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
     * Returns the command from this collection at the [path], or null if no command at that [path] was found.
     */
    fun getCommand(path: List<String>): Command<*>?

    /** Returns if this collection contains a command at the [path]. */
    fun containsCommand(path: List<String>): Boolean

    override fun iterator(): Iterator<Command<*>> = commands.iterator()
}
