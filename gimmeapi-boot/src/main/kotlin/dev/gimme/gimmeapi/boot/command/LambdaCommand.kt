package dev.gimme.gimmeapi.boot.command

import dev.gimme.gimmeapi.boot.command.executor.CommandExecutor
import dev.gimme.gimmeapi.command.node.CommandNode
import dev.gimme.gimmeapi.command.sender.CommandSender

/**
 * Represents a minimal [FunctionCommand] that is set up based on a supplied function.
 *
 * @property execute the function that this command is based on
 */
class LambdaCommand<T>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    val execute: (sender: CommandSender) -> T,
) : FunctionCommand<T>(
    name = name,
    parent = parent,
    aliases = aliases,
) {

    @CommandExecutor
    private fun foo(sender: CommandSender): T = execute(sender)
}
