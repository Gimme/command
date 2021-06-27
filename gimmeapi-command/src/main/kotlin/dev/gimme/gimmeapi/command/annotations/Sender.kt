package dev.gimme.gimmeapi.command.annotations

/**
 * Marks a command sender.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Sender
