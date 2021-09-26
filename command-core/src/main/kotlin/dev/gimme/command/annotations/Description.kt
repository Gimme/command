package dev.gimme.command.annotations

/**
 * Provides the description of the annotated parameter.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Description(
    /**
     * The description.
     */
    val value: String,
)
