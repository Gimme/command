package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import com.github.gimme.gimmebot.core.command.executor.tryExecuteCommandByReflection
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.kotlinFunction

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
        // Look through the public methods in the command class
        for (method in javaClass.methods) {
            val function = method.kotlinFunction ?: continue
            // Make sure it has the right annotation
            if (!function.hasAnnotation<CommandExecutor>()) continue

            val sb = StringBuilder(name)

            for (parameter in function.parameters.drop(1)) {
                sb.append(" ").append(parameter.name)
            }

            return sb.toString()
        }

        throw IllegalStateException("No function marked with @" + CommandExecutor::class.simpleName + " in the command \""
                + name + "\"")
    }
}
