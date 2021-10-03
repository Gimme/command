package dev.gimme.command.annotations

/**
 * Marks a command parameter.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Parameter(
    /**
     * The name of this parameter.
     */
    val value: String = "",

    /**
     * The description of this parameter.
     */
    val description: String = "",

    /**
     * The default value used if this parameter is optional.
     */
    val def: Default = Default("", ""),
)
