package com.github.gimme.gimmebot.boot.command

import com.github.gimme.gimmebot.boot.command.executor.CommandExecutor
import com.github.gimme.gimmebot.boot.command.executor.generateParameters
import com.github.gimme.gimmebot.boot.command.executor.generateUsage
import com.github.gimme.gimmebot.boot.command.executor.getFirstCommandExecutorFunction
import com.github.gimme.gimmebot.boot.command.executor.tryExecuteCommandByReflection
import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandParameterSet
import com.github.gimme.gimmebot.core.command.sender.CommandSender
import kotlin.reflect.full.findAnnotation

/**
 * Represents an easy-to-set-up command with base functionality.
 *
 * If a public method in this is marked with @[CommandExecutor], the command's [parameters] and [usage] are
 * automatically derived from it, and it gets called called when the command is [execute]d.
 *
 * @param T the response type
 */
abstract class BaseCommand<out T> @JvmOverloads constructor(
    final override val name: String,
    override val parent: Command<*>? = null,
    override var aliases: Set<String> = setOf(),
    override var summary: String = "",
    override var description: String = "",
) : Command<T> {

    init {
        require(name.isNotEmpty()) { "Command names cannot be empty" }
        require(!name.contains(" ")) { "Command names cannot contain spaces: \"$name\"" }
    }

    final override var parameters: CommandParameterSet
    final override var usage: String

    init {
        val function = getFirstCommandExecutorFunction()
        val commandExecutor: CommandExecutor = function.findAnnotation()!!

        parameters = generateParameters(function)
        usage = generateUsage(commandExecutor)
    }

    override fun execute(commandSender: CommandSender, args: List<String>): T {
        return tryExecuteCommandByReflection(this, commandSender, args)
    }

    override fun hashCode(): Int = id.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseCommand<*>

        if (id != other.id) return false

        return true
    }
}
