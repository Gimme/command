package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.sender.CommandSender

/**
 * Represents an executable command.
 *
 * @param T              the response type
 * @property name        the command name
 * @property parent      the optional parent command
 * @property aliases     aliases for the name
 * @property summary     a short summary of what this command does
 * @property description a detailed description of this command
 * @property usage       information of how to use the command
 * @property parameters  this command's parameters
 * @property isRoot      if this is a root command (no parent)
 */
interface Command<out T> {

    val name: String
    val parent: Command<*>?
    var aliases: Set<String>
    var summary: String
    var description: String
    var usage: String
    var parameters: CommandParameterSet

    val isRoot: Boolean get() = parent == null

    /**
     * Executes this command as the given [commandSender] with the given [args] and returns the response.
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    fun execute(commandSender: CommandSender, args: List<String>): T

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
