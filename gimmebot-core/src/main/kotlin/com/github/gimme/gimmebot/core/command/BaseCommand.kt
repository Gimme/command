package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.executor.getFirstCommandExecutorFunction
import com.github.gimme.gimmebot.core.command.executor.tryExecuteCommandByReflection

/**
 * Represents a command with base functionality.
 *
 * A public method marked with @[com.github.gimme.gimmebot.core.command.executor.CommandExecutor] is called when the
 * command is executed.
 */
abstract class BaseCommand(name: String) : Command {
    override val name: String = name.toLowerCase()

    override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
        return tryExecuteCommandByReflection(this, commandSender, args)
    }

    override fun getUsage(): String {
        val function = getFirstCommandExecutorFunction()

        val sb = StringBuilder(name)

        for (parameter in function.parameters.drop(1)) {
            sb.append(" ").append(parameter.name)
        }

        return sb.toString()
    }
}
