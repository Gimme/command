package dev.gimme.gimmeapi.command.manager.commandcollection

import dev.gimme.gimmeapi.command.Command
import dev.gimme.gimmeapi.command.CommandSearchResult

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

    /**
     * Searches for the command or node with the longest matching sub-set from the start of the [path] and returns the
     * result.
     */
    fun findCommand(path: List<String>): CommandSearchResult

    /**
     * Returns the roots of all child branches under the [path], or an empty set if the [path] does not exist.
     */
    fun getBranches(path: List<String>): Set<String>

    /**
     * Returns all leaves of all child branches under the command [path], or an empty set if no command exists under
     * that [path].
     */
    fun getLeafCommands(path: List<String>): Set<Command<*>>

    override fun iterator(): Iterator<Command<*>> = commands.iterator()
}
