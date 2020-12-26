package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandSender

/**
 * Represents a collection of commands.
 *
 * @param R the response type
 */
interface CommandCollection<R> {

    /** Adds the given [command] to this collection with the specified [responseParser] to convert the result. */
    fun <T> addCommand(command: Command<T>, responseParser: ((T) -> R)? = null)

    /**
     * Returns the command from this collection with the specified [name], or null if no command with that name is
     * registered.
     */
    fun getCommand(name: String): CommandNode<*, R>?

    /** Returns all commands in this collection. */
    fun getCommands(): List<Command<*>>

    /** Returns the command that best matches the start of the given [path], or null if no match. */
    fun findCommand(path: List<String>): CommandNode<*, R>?

    /**
     * Represents a wrapped [command] that can be executed with its response converted to a specific type, [R], through
     * the specified [responseParser]
     *
     * @param T the response type of the command
     * @param R the converted response type
     */
    data class CommandNode<T, R>(
        /** The wrapped command. */
        val command: Command<T>,
        /** The response converter. */
        val responseParser: ((T) -> R)?,
    ) {
        /**
         * Executes the wrapped [command] converting the response through the optional [responseParser] or else the
         * [defaultResponseParser].
         */
        fun execute(commandSender: CommandSender, args: List<String>, defaultResponseParser: (Any?) -> R): R {
            val response = command.execute(commandSender, args)
            return responseParser?.let { it(response) } ?: defaultResponseParser(response)
        }
    }
}
