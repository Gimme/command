package dev.gimme.command.annotations

/**
 * Provides the name of the annotated parameter.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Name(
    /**
     * The name.
     */
    val value: String,
)
