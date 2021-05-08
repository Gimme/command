package com.github.gimme.gimmebot.boot.command

import com.github.gimme.gimmebot.boot.command.executor.CommandExecutor
import com.github.gimme.gimmebot.boot.command.executor.generateParameters
import com.github.gimme.gimmebot.boot.command.executor.getDefaultValue
import com.github.gimme.gimmebot.boot.command.executor.getFirstCommandExecutorFunction
import com.github.gimme.gimmebot.boot.command.executor.tryExecuteCommandByReflection
import com.github.gimme.gimmebot.core.command.BaseCommand
import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.CommandParameterSet
import com.github.gimme.gimmebot.core.command.CommandSender
import kotlin.reflect.full.findAnnotation

/**
 * Represents an easy to set up command.
 *
 * A public method marked with @[CommandExecutor] is called when the
 * command is executed.
 *
 * @param T      the response type
 * @param name   the non-empty name of this command
 * @param parent the name of this command's optional parent
 */
abstract class SimpleCommand<out T>(name: String, parent: String? = null) : BaseCommand<T>(name, parent) {

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

    protected constructor(name: String, parent: Command<T>) : this(name, parent.name)

    override fun execute(commandSender: CommandSender, args: List<String>): T {
        return tryExecuteCommandByReflection(this, commandSender, args)
    }
}
