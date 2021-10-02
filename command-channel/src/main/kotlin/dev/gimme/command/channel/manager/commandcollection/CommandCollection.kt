package dev.gimme.command.channel.manager.commandcollection

import dev.gimme.command.Command
import dev.gimme.command.CommandSearchResult

/**
 * Represents a collection of commands.
 */
interface CommandCollection : Collection<Command<*>> {

    /** Returns all commands in this collection. */
    val commands: Set<Command<*>>

    /** Adds the given [command] to this collection. */
    fun add(command: Command<*>)

    /** Adds the given [commands] to this collection. */
    fun addAll(commands: Iterable<Command<*>>) = commands.forEach(this::add)

    /**
     * Returns the command from this collection at the [path], or null if no command at that [path] was found.
     */
    fun get(path: List<String>): Command<*>?

    /** Returns if this collection contains a command at the [path]. */
    fun contains(path: List<String>): Boolean = get(path) != null

    /**
     * Searches for the command or node with the longest matching sub-set from the start of the [path] and returns the
     * result.
     */
    fun find(path: List<String>): CommandSearchResult

    override fun iterator(): Iterator<Command<*>> = commands.iterator()

    override val size: Int get() = commands.size

    override fun isEmpty(): Boolean = commands.isEmpty()

    override fun contains(element: Command<*>): Boolean = commands.contains(element)

    override fun containsAll(elements: Collection<Command<*>>): Boolean = commands.containsAll(elements)
}
