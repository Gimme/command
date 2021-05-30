package com.github.gimme.gimmebot.core.command

import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.exception.ErrorCode

/**
 * Represents a command parameter type used to manage input argument values.
 *
 * @property name the display name of this parameter type
 * @property values gets all possible values this parameter type can have, or null if undefined
 */
abstract class CommandParameterType<T>(
    val name: String,
    val values: (() -> Set<String>)? = null,
) where T : Any {

    /**
     * Converts the [input] to this parameter type.
     *
     * @throws CommandException if the [input] is invalid
     */
    @Throws(CommandException::class)
    fun convert(input: Any): T = convertOrNull(input) ?: throw ErrorCode.INVALID_ARGUMENT.createException(input)

    /**
     * Converts the [input] to this parameter type, or null if failed to convert.
     *
     * @throws CommandException if the internal conversion failed
     */
    @Throws(CommandException::class)
    abstract fun convertOrNull(input: Any): T?
}
