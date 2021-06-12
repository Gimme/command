package dev.gimme.gimmeapi.command.parameter

/**
 * Holds information about a [CommandParameter]'s default value.
 *
 * @property value          the string version of the actual value to be used, or null if handled manually
 * @property representation the representation of the default value to be displayed. This is only for visual purposes
 * and is not used as an actual value.
 */
data class DefaultValue(
    val value: String?,
    val representation: String,
)
