package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.executor.CommandExecutor
import com.github.gimme.gimmebot.core.command.executor.getCommandDataParameters
import com.github.gimme.gimmebot.core.command.executor.getDefaultValue
import com.github.gimme.gimmebot.core.command.executor.getFirstCommandExecutorFunction
import com.github.gimme.gimmebot.core.command.executor.tryExecuteCommandByReflection
import org.apache.commons.lang3.StringUtils
import kotlin.reflect.full.findAnnotation

/**
 * Represents a command with base functionality.
 *
 * A public method marked with @[com.github.gimme.gimmebot.core.command.executor.CommandExecutor] is called when the
 * command is executed.
 *
 * @param T the response type
 */
abstract class BaseCommand<out T>(name: String) : Command<T> {

    override val name: String = name.toLowerCase()
    override val usage: String
        get() {
            val function = getFirstCommandExecutorFunction()
            val commandExecutor: CommandExecutor = function.findAnnotation()!!
            val sb = StringBuilder(name)

            getCommandDataParameters(function).forEachIndexed { index, parameter ->
                val defaultValue = getDefaultValue(commandExecutor, index)
                sb.append(" <${parameter.name?.splitCamelCase("-")}${defaultValue?.let { "=$defaultValue" } ?: ""}>")
            }

            return sb.toString()
        }

    override fun execute(commandSender: CommandSender, args: List<String>): T {
        return tryExecuteCommandByReflection(this, commandSender, args)
    }
}

/**
 * Converts this string from camel case to separate lowercase words separated by the specified [separator].
 */
internal fun String.splitCamelCase(separator: String): String =
    StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(this), separator)
        .toLowerCase()
        .replace("$separator $separator", separator)
