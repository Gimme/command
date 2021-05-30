package com.github.gimme.gimmebot.core.command

/**
 * Represents a command parameter, which helps define how arguments can be passed to the command execution function.
 *
 * @property id           the unique identifier of this parameter
 * @property displayName  the display name of this parameter
 * @property type         the type of this parameter
 * @property suggestions  suggested argument values
 * @property description  the description of this parameter
 * @property vararg       if this is a vararg parameter
 * @property optional     if this parameter is optional
 * @property flags        available shorthand flags representing this parameter
 * @property defaultValue the string representation of the default value used if this parameter is optional
 */
data class CommandParameter(
    val id: String,
    val displayName: String,
    val type: CommandParameterType<*>,
    val suggestions: Set<String> = setOf(),
    val description: String? = null,
    val vararg: Boolean = false,
    val optional: Boolean = false,
    val flags: Set<Char> = setOf(),
    val defaultValue: String? = null,
)
