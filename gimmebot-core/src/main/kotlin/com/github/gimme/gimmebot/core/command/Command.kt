package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.sender.CommandSender

/**
 * Represents an executable command.
 *
 * @param T              the response type
 * @property name        the command name (cannot contain spaces)
 * @property parent      the optional parent command
 * @property aliases     aliases for the name
 * @property summary     a short summary of what this command does
 * @property description a detailed description of this command
 * @property usage       information of how to use the command
 * @property parameters  this command's parameters
 * @property id          the id of this command (unique among commands with different paths)
 * @property root        the root command in the parent-chain
 * @property isRoot      if this is a root command (no parent)
 * @property path        the full [name]-path to this command
 * @property pathAliases all full paths to this command including [aliases]
 */
interface Command<out T> {

    val name: String
    val parent: Command<*>?
    var aliases: Set<String>
    var summary: String
    var description: String
    var usage: String
    var parameters: CommandParameterSet

    val id: String get() = path(" ")
    val root: Command<*> get() = parent?.root ?: this
    val isRoot: Boolean get() = parent == null
    val path: List<String> get() = (parent?.path ?: listOf()) + name

    val pathAliases: List<List<String>>
        get() {
            return parent?.let { parent ->
                parent.pathAliases.flatMap { parentPath ->
                    (aliases + name).map { alias ->
                        parentPath + alias
                    }
                }
            } ?: (aliases + name).map { listOf(it) }
        }

    /**
     * Executes this command as the given [commandSender] with the given [args] and returns the response.
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    fun execute(commandSender: CommandSender, args: List<String>): T

    /**
     * Returns the full [name]-path to this command including all [parent]s separated by the [separator].
     */
    fun path(separator: String): String = path.joinToString(" ")

    /**
     * Returns suggestions on the next input word based on already submitted [namedArgs] and amount of [orderedArgs]
     * already submitted.
     */
    fun getCompletionSuggestions(namedArgs: Set<String>, orderedArgs: Int): Set<String> {
        val unusedParameters: List<CommandParameter> =
            this.parameters.filter { !namedArgs.contains(it.id) }.drop(orderedArgs)
        val nextParameter: CommandParameter? = unusedParameters.firstOrNull()

        val suggestions = mutableSetOf<String>()

        nextParameter?.let { suggestions.addAll(it.suggestions) }
        unusedParameters.forEach { suggestions.addAll(it.getFlagAliases()) }

        return suggestions
    }

    private fun CommandParameter.getFlagAliases(): Set<String> {
        return mutableSetOf("--$id").apply { addAll(flags.map { "-$it" }) }
    }
}
