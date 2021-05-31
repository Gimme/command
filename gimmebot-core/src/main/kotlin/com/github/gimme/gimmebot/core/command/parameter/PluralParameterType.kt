package com.github.gimme.gimmebot.core.command.parameter

/**
 * A parameter type representing a group of values.
 *
 * @param R the real type that this represents
 */
class PluralParameterType<R>(
    name: String,
    override val values: (() -> Set<String>)? = null,
    private val convertFunction: (Collection<String>) -> R
) : ParameterType<R> {

    override val name = "[$name]"
    override val singular = false
    override val errorMessage: String? = null

    override fun convert(input: Collection<String>): R = convertFunction(input)
}
