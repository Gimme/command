package com.github.gimme.gimmebot.core.command.parameter

import kotlin.reflect.KType

/**
 * Represents a command parameter, which helps define how arguments can be passed to the command execution function.
 *
 * @property id           the unique identifier of this parameter
 * @property displayName  the display name of this parameter
 * @property type         the type of this parameter
 * @property suggestions  gets all suggested argument values
 * @property description  the description of this parameter
 * @property vararg       if this is a vararg parameter
 * @property optional     if this parameter is optional
 * @property flags        available shorthand flags representing this parameter
 * @property defaultValue the default value used if this parameter is optional
 */
open class CommandParameter(
    val id: String,
    val displayName: String,
    val type: KType,
    val suggestions: () -> Set<String> = { setOf() },
    val description: String? = null,
    val vararg: Boolean = false,
    val optional: Boolean = false,
    val flags: Set<Char> = setOf(),
    val defaultValue: DefaultValue? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandParameter

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
