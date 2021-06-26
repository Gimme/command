package dev.gimme.gimmeapi.command.annotations

import dev.gimme.gimmeapi.command.parameter.DefaultValue

/**
 * Marks a command parameter.
 *
 * @property default               the default value (in string form) used if this parameter is optional
 * @property defaultRepresentation the representation of the default value to be displayed, or null if nothing to be
 * displayed.
 * @property description           the description of this parameter
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
annotation class Parameter(
    @get:JvmName("defaultValue")
    val default: String = "",
    val defaultRepresentation: String = "",
    val description: String = "",
)

fun Parameter.getDefaultValue(): DefaultValue? {
    return if (default.isEmpty() && defaultRepresentation.isEmpty()) {
        null
    } else {
        DefaultValue(
            default.ifEmpty { null },
            defaultRepresentation.ifEmpty { null }
        )
    }
}
