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
 * @param T      the response type
 * @param name   the non-empty name of this command
 * @param parent the name of this command's optional parent
 */
abstract class BaseCommand<out T>(name: String, parent: String? = null) : Command<T> {

    final override val name: String
    override val usage: String by lazy {
        val function = getFirstCommandExecutorFunction()
        val commandExecutor: CommandExecutor = function.findAnnotation()!!
        val sb = StringBuilder(name)

        getCommandDataParameters(function).forEachIndexed { index, parameter ->
            val defaultValue = getDefaultValue(commandExecutor, index)
            sb.append(" <${parameter.name?.splitCamelCase("-")}${defaultValue?.let { "=$defaultValue" } ?: ""}>")
        }

        sb.toString()
    }

    init {
        require(name.isNotEmpty())
        this.name = (parent?.let { "$parent." } ?: "") + name.toLowerCase()
    }

    protected constructor(name: String, parent: Command<T>) : this(name, parent.name)

    override fun execute(commandSender: CommandSender, args: List<String>): T {
        return tryExecuteCommandByReflection(this, commandSender, args)
    }

    override fun hashCode(): Int = group.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseCommand<*>

        if (name != other.name) return false

        return true
    }
}

/**
 * Converts this string from camel case to separate lowercase words separated by the specified [separator].
 */
internal fun String.splitCamelCase(separator: String): String =
    StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(this), separator)
        .toLowerCase()
        .replace("$separator $separator", separator)
