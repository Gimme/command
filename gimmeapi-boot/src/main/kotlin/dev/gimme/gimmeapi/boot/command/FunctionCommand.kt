package dev.gimme.gimmeapi.boot.command

import dev.gimme.gimmeapi.boot.command.executor.CommandExecutor
import dev.gimme.gimmeapi.boot.command.executor.generateParameters
import dev.gimme.gimmeapi.boot.command.executor.generateUsage
import dev.gimme.gimmeapi.boot.command.executor.getFirstCommandExecutorFunction
import dev.gimme.gimmeapi.boot.command.executor.tryExecuteCommandByReflection
import dev.gimme.gimmeapi.command.BaseCommand
import dev.gimme.gimmeapi.command.node.CommandNode
import dev.gimme.gimmeapi.command.parameter.CommandParameter
import dev.gimme.gimmeapi.command.parameter.CommandParameterSet
import dev.gimme.gimmeapi.command.sender.CommandSender
import kotlin.reflect.full.findAnnotation

/**
 * Represents an easy-to-set-up command with automatic generation of some properties derived from a member function
 * marked with @[CommandExecutor].
 *
 * If a method in this is marked with @[CommandExecutor], the command's [parameters] and [usage] are automatically
 * derived from it, and it gets called called when the command is [execute]d.
 *
 * @param T the response type
 */
abstract class FunctionCommand<out T>(
    name: String,
    parent: CommandNode? = null,
    aliases: Set<String> = setOf(),
    summary: String = "",
    description: String = "",
) : BaseCommand<T>(
    name = name,
    parent = parent,
    aliases = aliases,
    summary = summary,
    description = description,
) {

    @JvmOverloads
    constructor(name: String, parent: CommandNode? = null) : this(name, parent, setOf())

    final override var parameters: CommandParameterSet
    final override var usage: String

    init {
        val function = getFirstCommandExecutorFunction()
        val commandExecutor: CommandExecutor = function.findAnnotation()!!

        parameters = generateParameters(function, commandExecutor)
        usage = generateUsage()
    }

    override fun execute(commandSender: CommandSender, args: Map<CommandParameter, Any?>): T {
        return tryExecuteCommandByReflection(this, commandSender, listOf() /* TODO */)
    }
}
