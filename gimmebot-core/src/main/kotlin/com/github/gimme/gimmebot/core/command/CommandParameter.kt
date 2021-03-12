package com.github.gimme.gimmebot.core.command

/**
 * Represents a command parameter, which helps define how arguments can be passed to the command execution function.
 *
 * @property id          the unique identifier of this parameter
 * @property displayName the display name of this parameter
 * @property type        the type of this parameter
 * @property suggestions suggested argument values
 * @property description the description of this parameter
 * @property vararg      if this is a vararg parameter
 * @property optional    if this parameter is optional
 * @property flags       available shorthand flags representing this parameter
 */
data class CommandParameter(
    val id: String,
    val displayName: String,
    val type: CommandParameterType<*>,
    val suggestions: Set<String> = setOf(),
    val description: String? = null,
    val vararg: Boolean = false,
    val optional: Boolean = false,
    val flags: Set<Char> = setOf()
)

/**
 * Represents a command parameter type used to manage input argument values.
 *
 * @property name the display name of this parameter type.
 */
abstract class CommandParameterType<T>(val name: String) where T : Any {

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
