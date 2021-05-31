package com.github.gimme.gimmebot.core.command.parameter

import com.github.gimme.gimmebot.core.command.exception.CommandException

/**
 * Represents a command parameter type used to handle input argument values.
 *
 * @param R                the real type that this represents
 * @property name          the display name of this parameter type
 * @property values        gets all possible values this parameter type can have, or null if undefined
 * @property singular      if this type represents singular or plural values
 * @property errorMessage  message to be included if an input value is invalid and cannot be converted to this type
 */
interface ParameterType<R> {

    val name: String
    val values: (() -> Set<String>)?
    val singular: Boolean
    val errorMessage: String?

    /**
     * Converts the [input] to this parameter type.
     *
     * @throws CommandException if the [input] is invalid
     */
    @Throws(CommandException::class)
    fun convert(input: Collection<String>): R
}
