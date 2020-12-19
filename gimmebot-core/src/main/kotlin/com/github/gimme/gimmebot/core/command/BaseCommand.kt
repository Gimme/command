package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.executor.getFirstCommandExecutorFunction
import com.github.gimme.gimmebot.core.command.executor.tryExecuteCommandByReflection
import org.apache.commons.lang3.StringUtils

/**
 * Converts this string from camel case to separate lowercase words with spaces.
 */
internal fun String.splitCamelCase(): String =
    StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(this), StringUtils.SPACE)
        .toLowerCase()
        .replace(" +".toRegex(), " ")

/**
 * Represents a command with base functionality.
 *
 * A public method marked with @[com.github.gimme.gimmebot.core.command.executor.CommandExecutor] is called when the
 * command is executed.
 */
abstract class BaseCommand(name: String) : Command {

    override val name: String = name.toLowerCase()
    override val usage: String
        get() {
            val function = getFirstCommandExecutorFunction()
            val sb = StringBuilder(name)

            for (parameter in function.parameters.drop(1)) {
                sb.append(" <${parameter.name?.splitCamelCase()}>")
            }

            return sb.toString()
        }

    override fun execute(commandSender: CommandSender, args: List<String>): CommandResponse? {
        return tryExecuteCommandByReflection(this, commandSender, args)
    }
}
