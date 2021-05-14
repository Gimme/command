package com.github.gimme.gimmebot.boot.command

import com.github.gimme.gimmebot.boot.command.executor.CommandExecutor
import com.github.gimme.gimmebot.boot.command.executor.generateParameters
import com.github.gimme.gimmebot.boot.command.executor.getDefaultValue
import com.github.gimme.gimmebot.boot.command.executor.getFirstCommandExecutorFunction
import com.github.gimme.gimmebot.boot.command.executor.tryExecuteCommandByReflection
import com.github.gimme.gimmebot.core.command.BaseCommand
import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandParameterSet
import com.github.gimme.gimmebot.core.command.sender.CommandSender
import kotlin.reflect.full.findAnnotation

/**
 * Represents an easy to set up text command.
 *
 * A public method marked with @[CommandExecutor] is called when the
 * command is executed.
 *
 * @param T the response type
 */
abstract class TextCommand<out T> @JvmOverloads constructor(
    name: String,
    aliases: Set<String> = setOf(),
    parentCommand: Command<*>? = null,
    summary: String = "",
    description: String = "",
) : BaseCommand<T>(
    name = parentCommand?.let { "${parentCommand.name} $name" } ?: name,
    aliases = parentCommand?.let {
        (parentCommand.aliases + parentCommand.name).flatMap { parentAlias ->
            (aliases + name).map { alias ->
                "$parentAlias $alias"
            }
        }.toSet().minus("${parentCommand.name} $name")
    } ?: aliases,
    summary = summary,
    description = description,
) {

    final override var usage: String
    final override var parameters: CommandParameterSet = generateParameters()

    init {
        val function = getFirstCommandExecutorFunction()
        val commandExecutor: CommandExecutor = function.findAnnotation()!!
        val sb = StringBuilder(name)

        parameters.forEachIndexed { index, parameter ->
            val defaultValue = commandExecutor.getDefaultValue(index)
            sb.append(" <${parameter.displayName}${defaultValue?.let { "=$defaultValue" } ?: ""}>")
        }

        this.usage = sb.toString()
    }

    override fun execute(commandSender: CommandSender, args: List<String>): T {
        return tryExecuteCommandByReflection(this, commandSender, args)
    }
}
