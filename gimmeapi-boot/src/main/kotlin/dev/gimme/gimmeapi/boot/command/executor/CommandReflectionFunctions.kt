package dev.gimme.gimmeapi.boot.command.executor

import dev.gimme.gimmeapi.command.parameter.DefaultValue

/**
 * Returns the default value for the parameter at the specified [index], or null if no default value for the specified
 * [index].
 *
 * Empty strings are treated and returned as null (no default value).
 */
internal fun CommandExecutor.getDefaultValue(index: Int): DefaultValue? {
    val value = this.defaultValues.getOrNull(index)?.let { if (it.isEmpty()) null else it }
    val representation = this.defaultValueRepresentations.getOrNull(index)?.let { if (it.isEmpty()) null else it }

    if (representation != null) return DefaultValue(value, representation)
    if (value != null) return DefaultValue(value, value)
    return null
}
