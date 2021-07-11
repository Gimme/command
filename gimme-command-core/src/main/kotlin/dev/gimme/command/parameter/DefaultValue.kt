package dev.gimme.command.parameter

/**
 * Holds information about a [CommandParameter]'s default value.
 *
 * @param value          the default value to be used
 * @param representation the representation of the default value to be displayed, or null if nothing to be displayed.
 * This is only for visual purposes and is not used as an actual value.
 */
data class DefaultValue(
    private val value: Any?,
    private val representation: String?,
) {

    /**
     * The default value as argument.
     */
    fun computeDefaultValue(): Any? = value

    /**
     * An descriptive string representing the value.
     *
     * Most of the time, this will just be the equivalent of value.toString().
     */
    fun getDisplayString(): String? = representation
}
