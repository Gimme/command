package dev.gimme.gimmeapi.core.command

import dev.gimme.gimmeapi.core.command.exception.CommandException
import dev.gimme.gimmeapi.core.command.node.CommandNode
import dev.gimme.gimmeapi.core.command.parameter.CommandParameter
import dev.gimme.gimmeapi.core.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.core.command.sender.CommandSender

/**
 * Represents an executable command.
 *
 * @param T              the response type
 * @property summary     a short summary of what this command does
 * @property description a detailed description of this command
 * @property usage       information of how to use the command
 * @property parameters  this command's parameters
 */
interface Command<out T> : CommandNode {

    var summary: String
    var description: String
    var usage: String
    var parameters: CommandParameterSet

    /**
     * Executes this command as the [commandSender] with the [args] mapping of parameters to arguments and returns the
     * result.
     *
     * @throws CommandException if the command execution was unsuccessful
     */
    @Throws(CommandException::class)
    fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): T

    // TODO: handle vararg parameter types
    /**
     * Returns suggestions on the next input word based on already submitted [namedArgs]/[flags] and amount of
     * [orderedArgs] already submitted.
     */
    fun getCompletionSuggestions(namedArgs: Set<String>, flags: Set<Char>, orderedArgs: Int): Set<String> {
        val unusedParameters: List<CommandParameter> =
            this.parameters
                .filter { !namedArgs.contains(it.id) && !flags.any { flag -> it.flags.contains(flag) } }
                .drop(orderedArgs)
        val nextParameter: CommandParameter? = unusedParameters.firstOrNull()

        val suggestions = mutableSetOf<String>()

        nextParameter?.let {
            it.defaultValue?.value?.let { defaultValue -> suggestions.add(defaultValue) }
            suggestions.addAll(it.suggestions())
        }
        unusedParameters.forEach { suggestions.addAll(it.getFlagAliases()) }

        return suggestions
    }

    private fun CommandParameter.getFlagAliases(): Set<String> {
        return mutableSetOf("--$id").apply { addAll(flags.map { "-$it" }) }
    }
}
