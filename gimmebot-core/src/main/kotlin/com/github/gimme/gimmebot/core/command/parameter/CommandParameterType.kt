package com.github.gimme.gimmebot.core.command.parameter

import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.exception.ErrorCode

/**
 * Represents a command parameter type used to manage input argument values.
 *
 * @property name          the display name of this parameter type
 * @property values        gets all possible values this parameter type can have, or null if undefined
 * @property errorMessage  message to be included if the input is invalid and cannot be converted to this type
 * @property convertOrNull converts the input to this parameter type, or null if failed to convert
 */
class CommandParameterType<T>(
    val name: String,
    val values: (() -> Set<String>)? = null,
    val errorMessage: String? = "Not a \"$name\"",
    var convertOrNull: (Any) -> T?
) where T : Any {

    /**
     * Converts the [input] to this parameter type.
     *
     * @throws CommandException if the [input] is invalid
     */
    @Throws(CommandException::class)
    fun convert(input: Any): T =
        convertOrNull(input)
            ?: throw ErrorCode.INVALID_ARGUMENT.createException("$input" + (errorMessage?.let { " ($it)" } ?: ""))
}
