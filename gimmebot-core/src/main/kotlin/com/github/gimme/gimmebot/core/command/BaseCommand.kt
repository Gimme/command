package com.github.gimme.gimmebot.core.command

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
}
