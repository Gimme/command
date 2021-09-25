package dev.gimme.command.function

import dev.gimme.command.annotations.Sender
import dev.gimme.command.node.CommandNode

/**
 * Represents a minimal [FunctionCommand] that is set up based on a supplied function.
 *
 * @param S the sender type
 * @param R the command result type
 * @property execute the function that this command is based on
 */
class LambdaCommand<S, R>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    val execute: (sender: S) -> R,
) : FunctionCommand<R>(
    name = name,
    parent = parent,
    aliases = aliases,
) {

    @CommandFunction
    private fun foo(@Sender sender: S): R = execute(sender)
}
