package com.github.gimme.gimmebot.core.command.parameter

import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.exception.ErrorCode

/**
 * A parameter type representing singular values.
 *
 * @param T the real type that this represents
 * @property convertOrNull converts string input to this parameter type, or returns null if unable to convert
 */
class SingularParameterType<T>(
    override val name: String,
    override val values: (() -> Set<String>)? = null,
    override val errorMessage: String? = "Not a `$name`",
    val convertOrNull: (String) -> T?
) : ParameterType<T> {

    override val singular = true

    /**
     * Converts the [input] to this parameter type.
     *
     * @throws CommandException if the [input] is invalid
     */
    @Throws(CommandException::class)
    fun convert(input: String): T = convertOrNull(input)
        ?: throw ErrorCode.INVALID_ARGUMENT.createException("\"$input\"${(errorMessage?.let { " ($it)" } ?: "")}")

    override fun convert(input: Collection<String>): R = convert(input.first())
}
