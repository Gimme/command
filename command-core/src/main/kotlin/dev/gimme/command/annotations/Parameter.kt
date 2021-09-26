package dev.gimme.command.annotations

/**
 * Marks a command parameter.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Parameter(
    /**
     * The description of this parameter.
     */
    val description: String = "",

    /**
     * The default value used if this parameter is optional.
     */
    val value: Default = Default("", ""),
)
